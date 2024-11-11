package com.routine.pusher.util;

import com.routine.pusher.model.dto.LembreteDTO;
import org.quartz.*;

import java.util.Date;

public final class JobScheduleUtils
{
    private JobScheduleUtils( ) {}

    public static JobDetail buildJobDetail( final Class jobClass, final LembreteDTO dto )
    {
        final JobDataMap map = new JobDataMap( );
        map.put( jobClass.getSimpleName( ), dto );

        return JobBuilder
                .newJob( jobClass )
                .withIdentity( jobClass.getSimpleName( ) )
                .setJobData( map )
                .build( );
    }


    public static Trigger buildTrigger( final Class jobClass, final LembreteDTO dto )
    {
        SimpleScheduleBuilder builder = SimpleScheduleBuilder.simpleSchedule()
                                        .withIntervalInHours(dto.getIntervalo().getHour());

        return TriggerBuilder
                .newTrigger( )
                .withIdentity( jobClass.getSimpleName( ) )
                .withSchedule( builder )
                .startAt( new Date(System.currentTimeMillis()) )
                .build( );
    }
}
