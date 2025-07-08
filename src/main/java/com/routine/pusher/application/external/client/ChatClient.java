package com.routine.pusher.application.external.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.routine.pusher.infrastructure.exceptions.ConversaoException;
import org.springframework.stereotype.Component;

@Component
public interface ChatClient<T>
{
    T buildLembreteChat( String frase ) throws JsonProcessingException, ConversaoException;
}
