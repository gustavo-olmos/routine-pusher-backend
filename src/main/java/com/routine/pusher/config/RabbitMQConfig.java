package com.routine.pusher.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig
{
    @Value("${rabbitmq.queue.name}")
    private String queue;

    @Value("${rabbitmq.queue.json}")
    private String jsonQueue;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Value("${rabbitmq.routing.json}")
    private String jsonRoutingKey;


    @Bean
    public Queue queue( )
    {
        return new Queue( queue );
    }

    @Bean
    public Queue jsonQueue( )
    {
        return new Queue( jsonQueue );
    }

    @Bean
    public TopicExchange exchange( )
    {
        return new TopicExchange( exchange );
    }

    @Bean
    public Binding binding( )
    {
        return BindingBuilder
                .bind( queue( ) )
                .to( exchange( ) )
                .with( routingKey );
    }

    @Bean
    public Binding jsonBinding( )
    {
        return BindingBuilder
                .bind( jsonQueue( ) )
                .to( exchange( ) )
                .with( jsonRoutingKey );
    }

    @Bean
    public MessageConverter converter( )
    {
        return new Jackson2JsonMessageConverter( );
    }

    public AmqpTemplate amqpTemplate( ConnectionFactory connectionFactory )
    {
        RabbitTemplate rabbitTemplate = new RabbitTemplate( connectionFactory );
        rabbitTemplate.setMessageConverter( converter( ) );
        return rabbitTemplate;
    }
}
