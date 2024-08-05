package com.routine.pusher.controller;

import com.routine.pusher.service.interfaces.NotificadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/v1/notificador")
public class NotificadorController
{
    private final NotificadorService service;

    public NotificadorController(NotificadorService service ) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> notificar( String token, String title, String body ) {
        service.notificar( token, title, body );

        return ResponseEntity.ok().body("Ok");
    }
}
