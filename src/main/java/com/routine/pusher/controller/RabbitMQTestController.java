package com.routine.pusher.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.routine.pusher.event.RabbitMQProducer;
import com.routine.pusher.model.dto.LembreteInputDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/rabbit")
public class RabbitMQTestController {

    @Autowired
    private RabbitMQProducer producer;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody LembreteInputDTO dto) throws JsonProcessingException {
        producer.sendMessage(dto);
        return ResponseEntity.ok("Message sent to RabbitMQ: ");
    }
}
