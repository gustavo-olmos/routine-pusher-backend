package com.routine.pusher.infrastructure.common.scheduler;

import com.routine.pusher.application.job.ExecutorJob;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.strategy.TriggerStrategy;
import com.routine.pusher.core.strategy.TriggerStrategyFactory;
import com.routine.pusher.infrastructure.exceptions.ExclusaoException;
import org.jetbrains.annotations.Nullable;
import org.quartz.*;

public final class QuartzScheduler<T>
{
    public QuartzScheduler( ){ }

    public JobDetail criarJob( T t, String key )
    {
        JobDataMap jobDataMap = new JobDataMap( );
        jobDataMap.put( key, t );

        return JobBuilder.newJob( ExecutorJob.class )
                         .withIdentity( key )
                         .setJobData( jobDataMap )
                         .build( );
    }

    public Trigger criarTrigger( T t )
    {
        TriggerStrategy<Lembrete> strategy = TriggerStrategyFactory.getStrategy( (Lembrete) t );
        return strategy.criarTrigger( (Lembrete) t);
    }

    @Nullable
    public T obterJob( JobDataMap jobDataMap, String jobId )
    {
        return (T) jobDataMap.get( jobId );
    }

    public void excluirJob( JobExecutionContext executionContext )
    {
        try {
            executionContext.getScheduler( ).deleteJob( executionContext.getJobDetail( ).getKey( ) );
        }
        catch ( SchedulerException ex ) {
            throw new ExclusaoException("Falha na remoção do job" + ex.getMessage( ));
        }
    }
}
