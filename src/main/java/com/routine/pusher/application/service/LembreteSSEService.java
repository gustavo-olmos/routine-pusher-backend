package com.routine.pusher.application.service;

import com.routine.pusher.application.usecase.NotificacaoUseCase;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.LembreteMapper;
import com.routine.pusher.core.domain.lembrete.LembreteRepository;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Um sink por sessão, e não um multicast global: com o global, todo inscrito recebia a notificação
 * de todo mundo — aceitável quando "todo mundo" era o próprio dev, vazamento entre visitantes agora
 * que cada um tem os seus lembretes.
 */
@Service
@AllArgsConstructor
public class LembreteSSEService implements NotificacaoUseCase<Lembrete>
{
    private final Map<UUID, Sinks.Many<String>> fluxos = new ConcurrentHashMap<>( );

    private final LembreteMapper mapper;
    private final LembreteRepository repository;

    @Override
    public Flux<String> obterFluxoNotificacoes( UUID sessaoUuid )
    {
        Sinks.Many<String> sink = fluxos.computeIfAbsent( sessaoUuid,
                uuid -> Sinks.many( ).multicast( ).onBackpressureBuffer( ) );

        // Quando o último inscrito da sessão desconecta, o sink sai do mapa: sessão que nunca mais
        // volta não deixa objeto órfão em memória. A remoção é condicionada à mesma instância para
        // não descartar um sink recém-criado por outra inscrição concorrente.
        return sink.asFlux( )
                .doFinally( signal -> {
                    if( sink.currentSubscriberCount( ) == 0 )
                        fluxos.remove( sessaoUuid, sink );
                } );
    }

    @Override
    public void adicionarEnvio( Lembrete lembrete )
    {
        Sinks.Many<String> sink = lembrete.getSessaoUuid( ) == null ? null
                : fluxos.get( lembrete.getSessaoUuid( ) );

        // Sem ouvinte não há para quem emitir, mas o disparo aconteceu do mesmo jeito: a última
        // execução é persistida sempre, senão a cota e o reagendamento divergiriam da realidade.
        if( sink != null )
            sink.tryEmitNext( lembrete.getTitulo( ) );

        Notificacao notificacao = lembrete.getNotificacao( );
        notificacao.setUltimaExecucao( LocalDateTime.now( ) );
        repository.save( mapper.toEntity( lembrete ) );
    }

    @Override
    public boolean possuiOuvinte( UUID sessaoUuid )
    {
        Sinks.Many<String> sink = fluxos.get( sessaoUuid );

        return sink != null && sink.currentSubscriberCount( ) > 0;
    }

    @Override
    public void encerrarFluxo( UUID sessaoUuid )
    {
        Sinks.Many<String> sink = fluxos.remove( sessaoUuid );

        if( sink != null )
            sink.tryEmitComplete( );
    }
}
