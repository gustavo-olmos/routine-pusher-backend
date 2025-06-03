package com.routine.pusher.infrastructure.common.helper;

import com.routine.pusher.application.job.ExecutorJob;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.infrastructure.common.helper.strategy.TriggerStrategy;
import com.routine.pusher.infrastructure.common.helper.strategy.TriggerStrategyFactory;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;

public final class AgendadorJobBuilder
{
    private AgendadorJobBuilder( ){ }

    public static JobDetail montarNovoJob( Lembrete lembrete )
    {
        JobDataMap jobDataMap = new JobDataMap( );
        jobDataMap.put( lembrete.getId( ).toString( ), lembrete );

        return JobBuilder.newJob( ExecutorJob.class )
                         .withIdentity( lembrete.getId( ).toString( ) )
                         .setJobData( jobDataMap )
                         .build( );
    }

    public static Trigger montarNovoTrigger( Lembrete lembrete )
    {
        TriggerStrategy strategy = TriggerStrategyFactory.getStrategy( lembrete );
        return strategy.criarTrigger( lembrete );
    }
}
