package com.routine.pusher.event;

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
    public void consume( LembreteDTO dto )
    {
        try {
            LOGGER.info("Trigger acionado, lembrete salvo para agendamento, {}", dto);
            service.agendar( dto );
        }
        catch ( Exception ex ) {
            LOGGER.error("Erro ao processar a mensagem, {}", ex.getMessage());
        }
    }

}