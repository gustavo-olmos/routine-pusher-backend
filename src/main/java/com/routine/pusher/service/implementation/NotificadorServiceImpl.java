package com.routine.pusher.service.implementation;

import com.routine.pusher.service.interfaces.NotificadorService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;

@Service
public class NotificadorServiceImpl implements NotificadorService, Job
{

    @Override
    public void notificar( String token, String title, String body ) {
//        Message message = Message.builder()
//                .setToken(token)
//                .setNotification(new Notification(title, body))
//                .build();
//
//        try {
//            String response = FirebaseMessaging.getInstance().send(message);
//            System.out.println("Successfully sent message: " + response);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void execute( JobExecutionContext context ) throws JobExecutionException
    {
        String token = context.getJobDetail().getJobDataMap().getString("token");
        String title = context.getJobDetail().getJobDataMap().getString("title");
        String body = context.getJobDetail().getJobDataMap().getString("body");

        notificar( token, title, body );
    }
}
