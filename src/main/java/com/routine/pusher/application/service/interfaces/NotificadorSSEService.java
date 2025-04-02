package com.routine.pusher.application.service.interfaces;

import reactor.core.publisher.Flux;

public interface NotificadorSSEService
{
    Flux<String> obterFluxoNotificacoes( );

    void adicionarEnvio( String message );
}
