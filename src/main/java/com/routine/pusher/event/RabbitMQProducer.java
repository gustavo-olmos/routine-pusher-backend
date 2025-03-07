package com.routine.pusher.event;

import com.routine.pusher.model.dto.LembreteDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQProducer.class);

    @Value("${rabbitmq.exchange.name}")
    private final String exchange;

    @Value("${rabbitmq.routing-key}")
    private final String routingKey;

    private final RabbitTemplate template;

    public RabbitMQProducer( String exchange, String routingKey, RabbitTemplate template )
    {
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.template = template;
    }


    public void sendMessage( LembreteDTO dto )
    {
        template.convertAndSend( exchange, routingKey, dto );

        LOGGER.info("Notificação enviada -> {}", dto);
    }
}
