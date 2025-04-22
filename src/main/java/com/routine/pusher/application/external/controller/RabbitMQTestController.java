package com.routine.pusher.application.external.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.infrastructure.message.RabbitMQProducer;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/v1/rabbit")
@Tag(name = "RabbitMQ", description = "Operações de teste da fila rabbit")
public class RabbitMQTestController {

    @Autowired
    private RabbitMQProducer producer;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage( @RequestBody LembreteOutputDTO dto ) throws JsonProcessingException
    {
        producer.sendMessage( dto );
        return ResponseEntity.ok("Message sent to RabbitMQ: ");
    }
}
