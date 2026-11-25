package com.routine.pusher.application.usecase;

import com.routine.pusher.core.domain.lembrete.dto.LembreteChatInputDTO;

public interface ChatUseCase<T>
{
    /**
     * @deprecated cria sem revisão do usuário. Use {@link InterpretacaoUseCase} e confirme pela
     * criação comum.
     */
    @Deprecated
    T criarLembreteViaChat( LembreteChatInputDTO entrada );
}
