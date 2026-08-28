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

import java.util.UUID;

public class LembreteExecutorJob implements Job
{
    private static final Logger LOGGER = LoggerFactory.getLogger( LembreteExecutorJob.class );

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
            LOGGER.error( "Lembrete não encontrado para o job ID: {}", jobId );
            return;
        }

        LOGGER.info( "Notificando job de id: {}", jobId );

        // Consome um disparo da cota ANTES do envio: adicionarEnvio persiste o lembrete, então o
        // decremento fica gravado e o limite é respeitado entre execuções (o job recarrega do banco).
        if( lembrete.getRecorrencia( ) != null )
            lembrete.getRecorrencia( ).consumirQuantidade( );

        useCase.adicionarEnvio( lembrete );

        reagendarOuRemover( executionContext, jobId, lembrete );
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
                context.getScheduler( ).deleteJob( context.getJobDetail( ).getKey( ) );
                LOGGER.info( "Job de id {} concluído e removido", jobId );
            }
        }
        catch ( SchedulerException e ) {
            LOGGER.error( "Erro ao reagendar/remover job {}", jobId, e );
        }
    }
}
