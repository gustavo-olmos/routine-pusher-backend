package com.routine.pusher.event.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.routine.pusher.event.RabbitMQProducer;
import com.routine.pusher.model.dto.LembreteDTO;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Component
public class PostgresListener
{
    private final Logger LOGGER = LoggerFactory.getLogger( PostgresListener.class );

    @Value("{$datasource.url}")
    private String urlConnection;

    @Value("{$datasource.username}")
    private String user;

    @Value("{$datasource.password}")
    private String passaword;

    private final RabbitMQProducer producer;

    public PostgresListener( RabbitMQProducer producer)
    {
        this.producer = producer;
    }


    @PostConstruct
    public void startListening( )
    {
        try {
            Connection connection = DriverManager.getConnection( urlConnection, user, passaword );

            Statement statement = connection.createStatement( );
            statement.execute( "LISTEN lembrete_channel;" );

            new Thread( ( ) -> {
                while( true ) {
                    try {
                        connection.createStatement().execute( "SELECT 1" );
                    }
                    catch (Exception e) {
                        LOGGER.error("Erro no try, {}", e.getMessage());
                    }
                }
            }).start( );
        }
        catch ( Exception e ) {
            LOGGER.error("Erro catch, {}", e.getMessage());
        }
    }

    private void handleNewLembrete( String lembreteJson ) throws JsonProcessingException
    {
        ObjectMapper objectMapper = new ObjectMapper( );
        producer.sendMessage( objectMapper.readValue( lembreteJson, LembreteDTO.class ) );
    }
}
