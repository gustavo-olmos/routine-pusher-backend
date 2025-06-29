package com.routine.pusher.application.usecase;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import reactor.core.publisher.Flux;

public interface NotificacaoUseCase
{
    Flux<String> obterFluxoNotificacoes( );

    void adicionarEnvio( Lembrete lembrete );
}
