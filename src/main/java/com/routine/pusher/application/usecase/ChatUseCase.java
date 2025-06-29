package com.routine.pusher.application.usecase;

import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;

public interface ChatUseCase
{
    LembreteOutputDTO criarLembreteViaChat( String frase );
}
