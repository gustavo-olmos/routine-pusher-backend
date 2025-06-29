package com.routine.pusher.application.job;

import com.routine.pusher.application.usecase.NotificacaoUseCase;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.notificacao.Notificacao;
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

    private final NotificacaoUseCase useCase;
    private final QuartzScheduler<Lembrete> quartz = new QuartzScheduler<>( );

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

        Notificacao notificacao = lembrete.getNotificacao( );
        if( notificacao.aindaTemNotificacao( lembrete ) )
            AgendadorJob.reagendar( executionContext, jobId, quartz.criarTrigger( lembrete ) );
    }

    private void notificar( String jobId, Lembrete lembrete )
    {
        LOGGER.info("Notificando job de id: {}", jobId);

        useCase.adicionarEnvio( lembrete );
    }
}