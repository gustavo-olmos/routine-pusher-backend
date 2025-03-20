package com.routine.pusher.application.service;

import com.routine.pusher.data.model.dto.LembreteOutputDTO;

public interface OpenAIChatService
{
    LembreteOutputDTO criarLembreteViaChat( String frase );
}
