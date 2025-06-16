package com.routine.pusher.application.service.interfaces;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import reactor.core.publisher.Flux;

public interface NotificadorSSEService
{
    Flux<String> obterFluxoNotificacoes( );

    void adicionarEnvio( Lembrete lembrete );
}
