package com.routine.pusher.util;

import com.routine.pusher.jobs.AgendadorJob;
import com.routine.pusher.model.dto.LembreteDTO;
import org.quartz.*;

import java.util.Date;

public final class AgendadorJobUtil
{
    private AgendadorJobUtil( ) {}

    public static JobDetail montarNovoJob(LembreteDTO dto )
    {
        JobDataMap jobDataMap = new JobDataMap( );
        jobDataMap.put( dto.getId( ).toString( ), dto );

        return JobBuilder.newJob( AgendadorJob.class )
                         .withIdentity( dto.getId( ).toString( ) )
                         .setJobData( jobDataMap )
                         .build( );
    }

    public static Trigger montarNovoTrigger(LembreteDTO dto )
    {
        //TODO: Usar aqui o intervalo de tempo complexo cronos
        SimpleScheduleBuilder builder = SimpleScheduleBuilder.simpleSchedule( )
                                        .withIntervalInHours( dto.getIntervalo( ).getMinute( ) );

        return TriggerBuilder.newTrigger( )
                             .withIdentity( dto.getId( ).toString( ) )
                             .withSchedule( builder )
                             .startAt( new Date( String.valueOf( dto.getIntervalo( ) ) ) )
                             .build( );
    }
}
