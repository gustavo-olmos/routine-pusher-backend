package com.routine.pusher.service.interfaces;

import org.quartz.SchedulerException;

public interface AgendadorService
{
    void scheduleJob( String jobName, String token, String title, String body, String cronExpression ) throws SchedulerException;

    void deleteJob( String jobName ) throws SchedulerException;
}
