package com.routine.pusher.controller;

import com.routine.pusher.event.RabbitMQProducer;
import com.routine.pusher.model.dto.LembreteDTO;
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


    //TODO: ISSO AQUI AINDA NÃO É AUTOMATIZAR A TAREFA, É CHAMAR POR ENDPOINT APENAS.
    //      PRECISA AMARRAR ISSO TUDO NO MOMENTO QUE ADICIONAMOS O LEMBRETE_DTO
    @GetMapping("/publicar")
    public ResponseEntity<String> sendMessage( @RequestParam("message") String message,
                                               Class jobClass, LembreteDTO dto )
    {
        service.notificar( jobClass, dto );
        producer.sendMessage( message );

        return ResponseEntity.ok( "Message sent to RabbitMQ... " );
    }
}