package com.routine.pusher.application.job;

import com.routine.pusher.application.usecase.NotificacaoUseCase;
import com.routine.pusher.core.domain.feriado.port.FeriadoPort;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.LembreteMapper;
import com.routine.pusher.core.domain.lembrete.LembreteRepository;
import com.routine.pusher.infrastructure.common.scheduler.QuartzScheduler;
import jakarta.inject.Inject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class LembreteExecutorJob implements Job
{
    private static final Logger LOGGER = LoggerFactory.getLogger( LembreteExecutorJob.class );

    /**
     * Atraso a partir do qual o disparo deixa de ser notificado.
     * <p>
     * Com job store persistente, tudo que venceu enquanto a aplicação esteve fora volta de uma vez
     * no boot — o Quartz chama isso de misfire e, por padrão, dispara na hora. Numa aplicação de
     * lembrete isso é uma enxurrada de notificações vencidas, e o valor de uma notificação atrasada
     * decai rápido.
     * <p>
     * O que <b>não</b> dá para fazer é mandar o Quartz descartar o misfire: os triggers daqui são de
     * disparo único e quem reagenda é este job. Trigger descartado = lembrete órfão para sempre.
     * Por isso o disparo acontece, e o filtro é aqui: o job roda, não notifica, e reagenda.
     */
    private static final Duration TOLERANCIA_ATRASO = Duration.ofMinutes( 5 );

    private final QuartzScheduler<Lembrete> quartz = new QuartzScheduler<>( );

    @Inject
    private NotificacaoUseCase<Lembrete> useCase;
    @Inject
    private LembreteRepository repository;
    @Inject
    private LembreteMapper mapper;
    @Inject
    private FeriadoPort feriados;

    @Override
    public void execute( JobExecutionContext executionContext )
    {
        String jobId = executionContext.getJobDetail( ).getKey( ).getName( );

        // A chave do job é o UUID do lembrete, não o id da tabela: estável mesmo que a base seja
        // recarregada — o que passa a importar quando o job store deixar de ser memória.
        Lembrete lembrete = repository.findByUuid( UUID.fromString( jobId ) )
                                      .map( mapper::toDomain )
                                      .orElse( null );
        if( lembrete == null ) {
            // Com job store persistente um trigger órfão é para sempre: ele dispara, não acha o
            // lembrete, loga e volta a ser agendado — repetindo o erro indefinidamente. Enquanto o
            // store era memória isso se resolvia sozinho no restart seguinte. Agora precisa ser
            // removido explicitamente.
            LOGGER.error( "Lembrete não encontrado para o job ID {}: removendo agendamento órfão", jobId );
            removerJob( executionContext, jobId );
            return;
        }

        if( disparoVencido( executionContext.getScheduledFireTime( ) ) ) {
            LOGGER.warn( "Disparo vencido do job {} (previsto para {}): reagendando sem notificar",
                    jobId, executionContext.getScheduledFireTime( ) );
        }
        else {
            LOGGER.info( "Notificando job de id: {}", jobId );

            // Consome um disparo da cota ANTES do envio: adicionarEnvio persiste o lembrete, então o
            // decremento fica gravado e o limite é respeitado entre execuções (o job recarrega do banco).
            if( lembrete.getRecorrencia( ) != null )
                lembrete.getRecorrencia( ).consumirQuantidade( );

            useCase.adicionarEnvio( lembrete );
        }

        reagendarOuRemover( executionContext, jobId, lembrete );
    }

    /**
     * Cota não é consumida em disparo vencido: o lembrete não foi entregue, então não faz sentido
     * cobrar dele.
     */
    boolean disparoVencido( Date horarioPrevisto )
    {
        if( horarioPrevisto == null ) return false;

        Duration atraso = Duration.between( horarioPrevisto.toInstant( ), Instant.now( ) );

        return atraso.compareTo( TOLERANCIA_ATRASO ) > 0;
    }

    private void removerJob( JobExecutionContext context, String jobId )
    {
        try {
            context.getScheduler( ).deleteJob( context.getJobDetail( ).getKey( ) );
        }
        catch ( SchedulerException e ) {
            LOGGER.error( "Erro ao remover job órfão {}", jobId, e );
        }
    }

    private void reagendarOuRemover( JobExecutionContext context, String jobId, Lembrete lembrete )
    {
        try {
            boolean temProximaNotificacao = lembrete.getNotificacao( ).aindaTemNotificacao( lembrete, feriados );
            boolean dentroDaCota = lembrete.getRecorrencia( ) == null
                    || lembrete.getRecorrencia( ).temQuantidadeRestante( );

            if( temProximaNotificacao && dentroDaCota ) {
                Trigger novoTrigger = quartz.criarTrigger( lembrete, feriados );
                context.getScheduler( ).rescheduleJob( new TriggerKey( jobId ), novoTrigger );
                LOGGER.info( "Reagendando job de id {}", jobId );
            }
            else {
                removerJob( context, jobId );
                LOGGER.info( "Job de id {} concluído e removido", jobId );
            }
        }
        catch ( SchedulerException e ) {
            LOGGER.error( "Erro ao reagendar/remover job {}", jobId, e );
        }
    }
}
