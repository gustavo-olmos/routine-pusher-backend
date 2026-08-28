package com.routine.pusher.application.usecase;

import com.routine.pusher.core.domain.sessao.dto.SessaoOutputDTO;

public interface SessaoUseCase
{
    /** A sessão da requisição corrente — o front usa para mostrar validade e oferecer o "esquecer-me". */
    SessaoOutputDTO atual( );

    /** Encerramento voluntário: o visitante pediu para ser esquecido agora, sem esperar a expiração. */
    void encerrar( );

    /**
     * Faxina das sessões expiradas por inatividade — disparada pelo relógio, não por um usuário,
     * mas é caso de uso do mesmo jeito: é a regra que decide o que morre e em que ordem.
     *
     * @return quantas sessões foram removidas
     */
    int removerExpiradas( );
}
