package com.routine.pusher.controller;

import com.routine.pusher.service.interfaces.AgendadorService;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public class AgendadorController
{
    @Autowired
    private AgendadorService service;

    @PostMapping("/schedule")
    public String scheduleNotification( @RequestParam String jobName, @RequestParam String token, @RequestParam String title,
                                        @RequestParam String body, @RequestParam String cronExpression )
    {
        try {
            service.scheduleJob( jobName, token, title, body, cronExpression );

            return "Notification scheduled!";
        }
        catch ( SchedulerException e ) {
            e.printStackTrace( );

            return "Error scheduling notification: " + e.getMessage( );
        }
    }

    @DeleteMapping("/delete")
    public String deleteNotification( @RequestParam String jobName )
    {
        try {
            service.deleteJob( jobName );

            return "Notification deleted!";
        }
        catch ( SchedulerException e ) {
            e.printStackTrace( );

            return "Error deleting notification: " + e.getMessage( );
        }
    }
}
