package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.usecase.NotificacaoUseCase;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/v1/notificar")
@Tag(name = "Notificador", description = "Operações WebFlux relacionadas à notificações")
public class NotificadorSSEController
{
    private final NotificacaoUseCase<Lembrete> useCase;
    private final SessaoAtualPort sessaoAtual;

    /**
     * A sessão vem do cookie — o SSE do navegador ({@code EventSource}) não envia header
     * customizado, e é exatamente por isso que a sessão anônima vive num cookie. A resolução
     * acontece aqui, ainda na thread da requisição: dentro do Flux já não haveria contexto.
     */
    @GetMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> subscribe( )
    {
        return useCase.obterFluxoNotificacoes( sessaoAtual.uuid( ) );
    }
}
