package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.service.interfaces.NotificadorSSEService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/v1/notificar")
public class NotificadorSSEController
{
    private final NotificadorSSEService service;

    @GetMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> subscribe( )
    {
        return service.obterFluxoNotificacoes( );
    }
}
