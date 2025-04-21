package com.routine.pusher.infrastructure.message;

import com.routine.pusher.application.job.AgendadorJob;
import com.routine.pusher.data.mapper.LembreteMapper;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
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

    private final SimpMessagingTemplate messagingTemplate;
    private final LembreteMapper mapper;

    @RabbitListener(queues = {"${rabbitmq.queue.name}"})
    public void consume( LembreteOutputDTO dto )
    {
        try {
            LOGGER.info("Trigger acionado, lembrete salvo para agendamento, {}", dto);
            AgendadorJob.agendar( mapper.toDomain( dto ) );
            messagingTemplate.convertAndSend( "/topic/notifications", dto );
        }
        catch ( Exception ex ) {
            LOGGER.error("Erro ao processar a mensagem, {}", ex.getMessage( ));
        }
    }

}