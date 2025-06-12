package com.routine.pusher.application.job;

import com.routine.pusher.application.service.interfaces.NotificadorSSEService;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.infrastructure.common.scheduler.QuartzScheduler;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@AllArgsConstructor
public class ExecutorJob implements Job
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorJob.class);

    private final QuartzScheduler<Lembrete> quartz = new QuartzScheduler<>( );
    private final NotificadorSSEService notificadorService;

    @Override
    public void execute( JobExecutionContext executionContext )
    {
        //TODO: REFATORAR EXECUÇÃO DE JOB:
        // AO EXECUTAR JOB -> NOTIFICAR -> VERIFICAR SE AINDA HÁ NOTIFICAÇÕES
        // REAGENDAR OU EXCLUIR JOB
        JobDataMap jobDataMap = executionContext.getJobDetail().getJobDataMap();
        String jobId = executionContext.getJobDetail().getKey().getName();

        Lembrete lembrete = quartz.obterJob(jobDataMap, jobId);
        if( lembrete == null ) {
            LOGGER.error("LembreteDTO não encontrado para o job ID: {}", jobId);
            return;
        }

        List<LocalDateTime> notificacoesAgendadas = lembrete.getDatasEspecificadas( );
        if ( notificacoesAgendadas.isEmpty( ) )
            quartz.excluirJob( executionContext );

        notificar( notificacoesAgendadas.get( 0 ), jobId, lembrete.getTitulo( ) );
        AgendadorJob.reagendar( executionContext, lembrete.getId( ).toString( ),
                quartz.criarTrigger( lembrete ) );
    }

    private void notificar( LocalDateTime proximaNotificacao, String jobId, String mensagem )
    {
        LocalDateTime agora = LocalDateTime.now( );
        if( !proximaNotificacao.isAfter( agora ) ) {
            LOGGER.info("Notificando job com id: {}", jobId);
            notificadorService.adicionarEnvio( mensagem );
        }
    }
}