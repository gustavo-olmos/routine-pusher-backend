package com.routine.pusher.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.routine.pusher.application.external.client.ChatClient;
import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.application.usecase.ChatUseCase;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.infrastructure.exceptions.ConversaoException;
import com.routine.pusher.infrastructure.exceptions.ProcessoException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OpenAIChatService implements ChatUseCase<LembreteOutputDTO>
{
    private final Logger LOGGER = LoggerFactory.getLogger( OpenAIChatService.class );

    private final ChatClient<LembreteInputDTO> client;
    private final CRUDUseCase<LembreteInputDTO, LembreteOutputDTO> useCase;

    @Override
    public LembreteOutputDTO criarLembreteViaChat( String frase )
    {
        LOGGER.debug("Iniciando chat..");

        try {
            return useCase.adicionar( client.buildLembreteChat( frase ) );
        }
        catch (JsonProcessingException | ConversaoException ex ) {
            throw new ProcessoException( "Falha na criação de lembrete via chat devido a conversão de dados. \n", ex.getMessage() );
        }
    }
}
