package com.routine.pusher.infrastructure.common.scheduler;

import com.routine.pusher.application.job.LembreteExecutorJob;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.trigger.TriggerCaseStrategy;
import com.routine.pusher.core.trigger.factory.TriggerStrategyFactory;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;

public final class QuartzScheduler<T>
{
    public QuartzScheduler( ){ }

    public JobDetail criarJob( String key )
    {
        return JobBuilder.newJob( LembreteExecutorJob.class )
                         .withIdentity( key )
                         .storeDurably( )
                         .build( );
    }

    public Trigger criarTrigger( T t )
    {
        TriggerCaseStrategy<Lembrete> caseStrategy = TriggerStrategyFactory.getStrategy( (Lembrete) t );
        return caseStrategy.criarTrigger( (Lembrete) t );
    }
}
