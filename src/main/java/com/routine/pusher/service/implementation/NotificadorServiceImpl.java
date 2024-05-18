package com.routine.pusher.service.implementation;

import com.routine.pusher.service.interfaces.NotificadorService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificadorServiceImpl implements NotificadorService
{

    @Override
    public String notificar( LocalDateTime tempo ) {
        return "";
    }
}
