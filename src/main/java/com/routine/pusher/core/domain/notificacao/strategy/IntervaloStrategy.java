package com.routine.pusher.core.domain.notificacao.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.strategy.ProximaNotificacaoStrategy;

import java.time.Duration;
import java.time.LocalDateTime;

public class IntervaloStrategy implements ProximaNotificacaoStrategy<Lembrete>
{
    @Override
    public LocalDateTime calcular( Lembrete lembrete )
    {
        Recorrencia recorrencia = lembrete.getRecorrencia( );
        Notificacao notificacao = lembrete.getNotificacao( );
        if( recorrencia == null || notificacao == null ) return null;

        Duration intervalo = recorrencia.montarIntevalo( );
        LocalDateTime base = ( notificacao.getUltimaExecucao( ) != null )
                ? notificacao.getUltimaExecucao( )
                : notificacao.getDataInicio( );

        return base.plus( intervalo );
    }
}
