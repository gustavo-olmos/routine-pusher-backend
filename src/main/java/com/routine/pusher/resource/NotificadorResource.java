package com.routine.pusher.resource;

import com.routine.pusher.service.interfaces.NotificadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping(path = "api/v1/notificador")
public class NotificadorResource
{
    private NotificadorService service;

    public NotificadorResource( NotificadorService service ) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity notificar( LocalDateTime tempo ) {
        return ResponseEntity.ok( ).body( service.notificar( tempo ) );
    }
}
