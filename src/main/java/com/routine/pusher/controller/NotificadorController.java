package com.routine.pusher.controller;

import com.routine.pusher.event.RabbitMQProducer;
import com.routine.pusher.service.interfaces.NotificadorService;
import org.quartz.JobDataMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/notificador")
public class NotificadorController
{
    private final NotificadorService service;
    private final RabbitMQProducer producer;

    public NotificadorController( NotificadorService service, RabbitMQProducer producer )
    {
        this.service = service;
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<String> notificar( String title, String body )
    {
        service.notificar( title, body );

        return ResponseEntity.ok().body("Ok");
    }

    @GetMapping("/publish")
    public ResponseEntity<String> sendMessage( @RequestParam("message") String message )
    {
        producer.sendMessage( message );

        return ResponseEntity.ok( "Message sent to RabbitMQ... " );
    }
}
