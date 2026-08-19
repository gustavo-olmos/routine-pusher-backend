package com.routine.pusher.application.job;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.infrastructure.common.scheduler.QuartzScheduler;
import com.routine.pusher.infrastructure.exceptions.ProcessoException;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AgendadorJob
{
    private static final Logger LOGGER = LoggerFactory.getLogger( AgendadorJob.class );

    private final Scheduler scheduler;
    private final QuartzScheduler<Lembrete> quartz = new QuartzScheduler<>( );

    public AgendadorJob( Scheduler scheduler )
    {
        this.scheduler = scheduler;
    }

    public void agendar( Lembrete lembrete )
    {
        JobDetail jobDetail = quartz.criarJob( lembrete.getId( ).toString( ) );
        Trigger trigger = quartz.criarTrigger( lembrete );

        try {
            scheduler.scheduleJob( jobDetail, trigger );
        }
        catch ( SchedulerException e ) {
            LOGGER.error( e.getMessage( ), e );
            throw new ProcessoException( "Agendamento de lembrete", e.getMessage( ) );
        }
    }
}
