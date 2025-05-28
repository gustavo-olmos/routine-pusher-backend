package com.routine.pusher.application.external.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.infrastructure.exceptions.ConversaoException;
import org.springframework.stereotype.Component;

@Component
public interface OpenAIChatClient
{
    LembreteInputDTO buildLembreteChat(String frase ) throws JsonProcessingException, ConversaoException;
}
