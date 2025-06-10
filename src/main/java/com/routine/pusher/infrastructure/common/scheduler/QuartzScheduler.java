package com.routine.pusher.infrastructure.common.scheduler;

import com.routine.pusher.application.job.ExecutorJob;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.strategy.TriggerStrategy;
import com.routine.pusher.core.strategy.TriggerStrategyFactory;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;

public final class QuartzScheduler<T>
{
    public QuartzScheduler( ){ }

    public JobDetail montarNovoJob( T t, String key )
    {
        JobDataMap jobDataMap = new JobDataMap( );
        jobDataMap.put( key, t );

        return JobBuilder.newJob( ExecutorJob.class )
                         .withIdentity( key )
                         .setJobData( jobDataMap )
                         .build( );
    }

    public Trigger montarNovoTrigger( T t )
    {
        TriggerStrategy strategy = TriggerStrategyFactory.getStrategy( (Lembrete) t );
        return strategy.criarTrigger( t );
    }
}
