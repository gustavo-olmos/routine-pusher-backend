package com.routine.pusher.application.service.interfaces;

import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;

public interface OpenAIChatService
{
    LembreteOutputDTO criarLembreteViaChat( String frase );
}
