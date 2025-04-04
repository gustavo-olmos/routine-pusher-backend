package com.routine.pusher.application.external.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.routine.pusher.data.model.dto.LembreteInputDTO;
import com.routine.pusher.infrastructure.exceptions.ConversaoLembreteException;
import org.springframework.stereotype.Component;

@Component
public interface OpenAIChatClient
{
    LembreteInputDTO buildLembreteChat(String frase ) throws JsonProcessingException, ConversaoLembreteException;
}
