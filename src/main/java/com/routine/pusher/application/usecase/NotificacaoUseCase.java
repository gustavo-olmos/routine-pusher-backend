package com.routine.pusher.application.usecase;

import reactor.core.publisher.Flux;

public interface NotificacaoUseCase<T>
{
    Flux<String> obterFluxoNotificacoes( );

    void adicionarEnvio( T t );
}
