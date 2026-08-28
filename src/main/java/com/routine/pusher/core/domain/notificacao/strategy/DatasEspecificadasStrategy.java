package com.routine.pusher.core.domain.notificacao.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.notificacao.Notificacao;

import java.time.LocalDateTime;
import java.util.List;

public class DatasEspecificadasStrategy implements NotificacaoCaseStrategy<Lembrete>
{
    @Override
    public LocalDateTime calcularProximaNotificacao(Lembrete lembrete )
    {
        Notificacao notificacao = lembrete.getNotificacao( );
        List<LocalDateTime> datasFuturas = datasFuturas( notificacao );

        if( datasFuturas.isEmpty( ) ) return null;

        notificacao.setDataFim( datasFuturas.get( datasFuturas.size( ) - 1 ) );
        return datasFuturas.get( 0 );
    }

    @Override
    public List<LocalDateTime> calcularProximasNotificacoes( Lembrete lembrete, int limite )
    {
        return datasFuturas( lembrete.getNotificacao( ) ).stream( )
                .limit( limite )
                .toList( );
    }

    private List<LocalDateTime> datasFuturas( Notificacao notificacao )
    {
        if( notificacao == null || notificacao.getDatasEspecificadas( ) == null ) return List.of( );

        return notificacao.getDatasEspecificadas( ).stream( )
                .filter( data -> data.isAfter( LocalDateTime.now( ) ) )
                .sorted( )
                .toList( );
    }
}
