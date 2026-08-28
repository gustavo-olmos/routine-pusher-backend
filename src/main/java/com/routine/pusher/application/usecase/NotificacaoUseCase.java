package com.routine.pusher.application.usecase;

import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Notificação é assunto de uma sessão, não da aplicação inteira: cada visitante escuta só o próprio
 * fluxo — por isso todo método carrega o UUID da sessão dona.
 */
public interface NotificacaoUseCase<T>
{
    Flux<String> obterFluxoNotificacoes( UUID sessaoUuid );

    void adicionarEnvio( T t );

    /** Há alguém conectado ao fluxo desta sessão agora? A faxina usa isto como sinal de presença. */
    boolean possuiOuvinte( UUID sessaoUuid );

    /** Fecha e descarta o fluxo da sessão — chamado quando a sessão deixa de existir. */
    void encerrarFluxo( UUID sessaoUuid );
}
