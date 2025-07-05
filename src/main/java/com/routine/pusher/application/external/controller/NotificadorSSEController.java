package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.usecase.NotificacaoUseCase;
import com.routine.pusher.core.domain.lembrete.Lembrete;
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

    @GetMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> subscribe( )
    {
        return useCase.obterFluxoNotificacoes( );
    }
}
