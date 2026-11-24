package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.usecase.NotificacaoUseCase;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/v1/notificar")
@Tag(name = "Notificador", description = "Operações WebFlux relacionadas à notificações")
public class NotificadorSSEController
{
    /**
     * Folga contra o idle timeout de 60s do balanceador do Lightsail: um batimento perdido ainda
     * deixa o seguinte chegar a tempo.
     */
    static final Duration BATIMENTO = Duration.ofSeconds( 20 );

    private final NotificacaoUseCase<Lembrete> useCase;
    private final SessaoAtualPort sessaoAtual;

    /**
     * A sessão vem do cookie — o SSE do navegador ({@code EventSource}) não envia header
     * customizado, e é exatamente por isso que a sessão anônima vive num cookie. A resolução
     * acontece aqui, ainda na thread da requisição: dentro do Flux já não haveria contexto.
     *
     * <p>O batimento existe por causa do balanceador, não do navegador. Sem ele o servidor não
     * escreve nada até o primeiro disparo, e os cabeçalhos só saem junto com o primeiro byte: o
     * balanceador espera 60s, desiste e devolve <b>504</b>. Um {@code EventSource} que recebe status
     * diferente de 200 fecha de vez, sem reconectar. Foi o que aconteceu em produção: o fluxo
     * nunca chegava a abrir. O primeiro batimento sai em {@link Duration#ZERO} justamente para
     * mandar os cabeçalhos na hora. Os seguintes mantêm a conexão viva e fazem um cliente morto
     * aparecer como falha de escrita, que cancela a inscrição.
     *
     * <p>Batimento é comentário SSE ({@code :ping}): o {@code EventSource} descarta, então o
     * {@code onmessage} do front continua recebendo só títulos.
     */
    @GetMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> subscribe( )
    {
        // O intervalo nunca termina sozinho. Sem amarrá-lo ao fim dos avisos, a sessão encerrada
        // pela faxina (encerrarFluxo) deixaria a conexão aberta só com batimentos.
        Sinks.Empty<Void> fim = Sinks.empty( );

        Flux<ServerSentEvent<String>> avisos = useCase.obterFluxoNotificacoes( sessaoAtual.uuid( ) )
                .map( titulo -> ServerSentEvent.builder( titulo ).build( ) )
                .doOnTerminate( fim::tryEmitEmpty );

        Flux<ServerSentEvent<String>> batimentos = Flux.interval( Duration.ZERO, BATIMENTO )
                .map( n -> ServerSentEvent.<String>builder( ).comment( "ping" ).build( ) )
                .takeUntilOther( fim.asMono( ) );

        return Flux.merge( avisos, batimentos );
    }
}
