package com.routine.pusher.infrastructure.message;

import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQProducer.class);

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Autowired
    private RabbitTemplate template;


    public void sendMessage( LembreteOutputDTO dto )
    {
        try {
            template.convertAndSend( exchange, routingKey, dto );
            LOGGER.info("Notificação enviada -> {}", dto);
        }
        catch ( Exception ex ) {
            LOGGER.error("Falha na conversão e envio da notificação pelo producer, {}", ex.getMessage( ));
        }

    }
}
