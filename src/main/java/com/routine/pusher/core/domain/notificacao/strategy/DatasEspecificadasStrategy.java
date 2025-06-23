package com.routine.pusher.core.domain.notificacao.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.strategy.ProximaNotificacaoStrategy;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class DatasEspecificadasStrategy implements ProximaNotificacaoStrategy<Lembrete>
{
    @Override
    public LocalDateTime calcular( Lembrete lembrete )
    {
        Notificacao notificacao = lembrete.getNotificacao( );
        List<LocalDateTime> datasFuturas = notificacao.getDatasEspecificadas( ).stream( )
                .filter( d -> d.isAfter( LocalDateTime.now( ) ) )
                .sorted( )
                .toList( );

        if( datasFuturas.isEmpty( ) ) return null;

        notificacao.setDataFim( Collections.max( datasFuturas ) );
        return datasFuturas.get( 0 );
    }
}
