package com.routine.pusher.application.service.interfaces;

import com.routine.pusher.data.model.dto.LembreteOutputDTO;

public interface OpenAIChatService
{
    LembreteOutputDTO criarLembreteViaChat( String frase );
}
