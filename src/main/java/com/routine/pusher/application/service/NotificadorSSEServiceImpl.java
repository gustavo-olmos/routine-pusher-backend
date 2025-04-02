package com.routine.pusher.application.service;

import com.routine.pusher.application.service.interfaces.NotificadorSSEService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
@AllArgsConstructor
public class NotificadorSSEServiceImpl implements NotificadorSSEService
{
    private final Sinks.Many<String> sink = Sinks.many( ).multicast( ).onBackpressureBuffer( );


    @Override
    public Flux<String> obterFluxoNotificacoes( )
    {
        return sink.asFlux( );
    }

    @Override
    public void adicionarEnvio( String message )
    {
        sink.tryEmitNext( message );
    }
}
