package com.routine.pusher.service.implementation;

import com.routine.pusher.service.interfaces.NotificadorService;
import org.springframework.stereotype.Service;

@Service
public class NotificadorServiceImpl implements NotificadorService
{

    @Override
    public void notificar( String token, String title, String body ) {
    }
}
