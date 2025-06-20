package com.routine.pusher.application.job;

import com.routine.pusher.application.service.interfaces.NotificadorSSEService;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.infrastructure.common.scheduler.QuartzScheduler;
import lombok.AllArgsConstructor;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
        JobDataMap jobDataMap = executionContext.getJobDetail( ).getJobDataMap( );
        String jobId = executionContext.getJobDetail( ).getKey( ).getName( );

        Lembrete lembrete = quartz.obterJob( jobDataMap, jobId, Lembrete.class );
        if( lembrete == null ) {
            LOGGER.error("LembreteDTO não encontrado para o job ID: {}", jobId);
            return;
        }

        notificar( jobId, lembrete );
        quartz.excluirJob( executionContext );

        if( lembrete.aindaTemNotificacao( ) )
            AgendadorJob.reagendar( executionContext, jobId, quartz.criarTrigger( lembrete ) );
    }

    private void notificar( String jobId, Lembrete lembrete )
    {
        LOGGER.info("Notificando job com id: {}", jobId);

        notificadorService.adicionarEnvio( lembrete );
    }
}