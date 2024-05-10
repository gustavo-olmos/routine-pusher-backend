package com.routine.pusher.service.implementation;

import com.routine.pusher.model.Tempo;
import com.routine.pusher.service.NotificadorService;
import org.springframework.stereotype.Service;

@Service
public class NotificadorServiceImpl implements NotificadorService
{
    private String destinario;

    public NotificadorServiceImpl( String destinario )
    {
        this.destinario = destinario;
    }

    @Override
    public String notificar(Tempo tempo) { return ""; }
}
