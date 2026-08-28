package com.routine.pusher.application.usecase;

import com.routine.pusher.core.domain.lembrete.dto.LembreteChatInputDTO;

public interface ChatUseCase<T>
{
    T criarLembreteViaChat( LembreteChatInputDTO entrada );
}
