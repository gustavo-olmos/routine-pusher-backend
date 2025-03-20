package com.routine.pusher.application.service.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.routine.pusher.application.interfaces.client.OpenAIChatClient;
import com.routine.pusher.application.service.LembreteService;
import com.routine.pusher.application.service.OpenAIChatService;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.infrastructure.exceptions.ConversaoLembreteException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OpenAIChatServiceImpl implements OpenAIChatService
{
    private final Logger LOGGER = LoggerFactory.getLogger( OpenAIChatServiceImpl.class );

    private final OpenAIChatClient client;
    private final LembreteService service;

    @Override
    public LembreteOutputDTO criarLembreteViaChat( String frase )
    {
        LOGGER.debug("Iniciando chat..");

        try {
            return service.adicionar( client.buildLembreteChat( frase ) );
        }
        catch (JsonProcessingException | ConversaoLembreteException ex ) {
            throw new RuntimeException( ex );
        }
    }
}
