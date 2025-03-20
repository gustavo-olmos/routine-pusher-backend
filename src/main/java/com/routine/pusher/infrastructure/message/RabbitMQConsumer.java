package com.routine.pusher.infrastructure.message;

import com.routine.pusher.data.model.dto.LembreteInputDTO;
import com.routine.pusher.application.service.AgendadorService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class RabbitMQConsumer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQConsumer.class);

    private final AgendadorService service;
    private final SimpMessagingTemplate messagingTemplate;


    @RabbitListener(queues = {"${rabbitmq.queue.name}"})
    public void consume( LembreteInputDTO dto )
    {
        try {
            LOGGER.info("Trigger acionado, lembrete salvo para agendamento, {}", dto);
            service.agendar( dto );
            messagingTemplate.convertAndSend( "/topic/notifications", dto );
        }
        catch ( Exception ex ) {
            LOGGER.error("Erro ao processar a mensagem, {}", ex.getMessage( ));
        }
    }

}