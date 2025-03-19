package com.routine.pusher.event;

import com.routine.pusher.model.dto.LembreteInputDTO;
import com.routine.pusher.service.interfaces.AgendadorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
public class RabbitMQConsumer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQConsumer.class);

    private final AgendadorService service;
    private final SimpMessagingTemplate messagingTemplate;

    public RabbitMQConsumer( AgendadorService service, SimpMessagingTemplate messagingTemplate )
    {
        this.service = service;
        this.messagingTemplate = messagingTemplate;
    }


    @RabbitListener(queues = {"${rabbitmq.queue.name}"})
    public void consume( LembreteInputDTO dto )
    {
        try {
            LOGGER.info("Trigger acionado, lembrete salvo para agendamento, {}", dto);
            service.agendar( dto );
            messagingTemplate.convertAndSend( "/topic/notifications", dto );
        }
        catch ( Exception ex ) {
            LOGGER.error("Erro ao processar a mensagem, {}", ex.getMessage());
        }
    }

}