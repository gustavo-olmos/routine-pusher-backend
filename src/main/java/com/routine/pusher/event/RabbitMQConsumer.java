package com.routine.pusher.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.service.interfaces.AgendadorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


@Service
public class RabbitMQConsumer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQConsumer.class);

    private final AgendadorService service;

    public RabbitMQConsumer( AgendadorService service )
    {
        this.service = service;
    }

    @RabbitListener(queues = {"${rabbitmq.queue.name}"})
    public void consume( String message ) throws JsonProcessingException
    {
        LOGGER.info("Received message -> {}", message);

        ObjectMapper objectMapper = new ObjectMapper();
        LembreteDTO lembrete = objectMapper.readValue( message, LembreteDTO.class );

        service.agendar( lembrete );
    }

}
