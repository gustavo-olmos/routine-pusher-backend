package com.routine.pusher.service.implementation;

import com.routine.pusher.service.interfaces.AgendadorService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;

public class AgendadorServiceImpl implements AgendadorService
{
    @Autowired
    private Scheduler scheduler;


    @Override
    public void scheduleJob( String jobName, String token, String title, String body, String cronExpression ) throws SchedulerException
    {
        JobDetail jobDetail = JobBuilder.newJob( NotificadorServiceImpl.class )
                .withIdentity( jobName )
                .usingJobData( "token", token )
                .usingJobData( "title", title )
                .usingJobData( "body", body )
                .build( );

        Trigger trigger = TriggerBuilder.newTrigger( )
                .withIdentity( jobName + "Trigger" )
                .withSchedule( CronScheduleBuilder.cronSchedule( cronExpression ) )
                .build( );

        scheduler.scheduleJob( jobDetail, trigger );
    }

    @Override
    public void deleteJob( String jobName ) throws SchedulerException
    {
        JobKey jobKey = new JobKey( jobName );
        scheduler.deleteJob( jobKey );
    }
}
