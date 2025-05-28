package com.routine.pusher.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.routine.pusher.application.external.client.OpenAIChatClient;
import com.routine.pusher.application.service.interfaces.LembreteService;
import com.routine.pusher.application.service.interfaces.OpenAIChatService;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.infrastructure.exceptions.ConversaoException;
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
            return service.salvar( client.buildLembreteChat( frase ) );
        }
        catch (JsonProcessingException | ConversaoException ex ) {
            throw new RuntimeException( ex );
        }
    }
}
