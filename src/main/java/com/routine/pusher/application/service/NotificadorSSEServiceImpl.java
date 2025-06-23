package com.routine.pusher.application.service;

import com.routine.pusher.application.service.interfaces.NotificadorSSEService;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.LembreteMapper;
import com.routine.pusher.core.domain.lembrete.LembreteRepository;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class NotificadorSSEServiceImpl implements NotificadorSSEService
{
    private final Sinks.Many<String> sink = Sinks.many( ).multicast( ).onBackpressureBuffer( );

    private final LembreteMapper mapper;
    private final LembreteRepository repository;

    @Override
    public Flux<String> obterFluxoNotificacoes( )
    {
        return sink.asFlux( );
    }

    @Override
    public void adicionarEnvio( Lembrete lembrete )
    {
        sink.tryEmitNext( lembrete.getTitulo( ) );

        Notificacao notificacao = lembrete.getNotificacao( );
        notificacao.setUltimaExecucao( LocalDateTime.now( ) );
        repository.save( mapper.toEntity( lembrete ) );
    }
}
