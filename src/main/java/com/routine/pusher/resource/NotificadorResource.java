package com.routine.pusher.resource;

import com.routine.pusher.model.Tempo;
import com.routine.pusher.service.interfaces.NotificadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NotificadorResource
{
    private NotificadorService service;

    public NotificadorResource( NotificadorService service ) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity notificar( Tempo tempo ) {
        return ResponseEntity.ok( ).body( service.notificar( tempo ) );
    }
}
