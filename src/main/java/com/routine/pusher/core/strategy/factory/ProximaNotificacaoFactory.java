package com.routine.pusher.core.strategy.factory;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.domain.notificacao.strategy.CronStrategy;
import com.routine.pusher.core.domain.notificacao.strategy.DatasEspecificadasStrategy;
import com.routine.pusher.core.domain.notificacao.strategy.IntervaloStrategy;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.strategy.ProximaNotificacaoStrategy;

import java.time.Duration;

public class ProximaNotificacaoFactory
{
    public static ProximaNotificacaoStrategy<Lembrete> getStrategy( Lembrete lembrete )
    {
        if( lembrete == null ) return null;

        Notificacao notificacao = lembrete.getNotificacao( );
        if( notificacao.getDatasEspecificadas( ) != null && !notificacao.getDatasEspecificadas( ).isEmpty( ) )
            return new DatasEspecificadasStrategy( );

        Recorrencia recorrencia = lembrete.getRecorrencia( );
        if( recorrencia != null ) {
            boolean temCronExpression = !recorrencia.getDiasDaSemana( ).isEmpty( ) ||
                    !recorrencia.getDiasFixosNoMes( ).isEmpty( ) ||
                    recorrencia.getPosicaoDaSemanaNoMes( ) > 0;

            if( temCronExpression )
                return new CronStrategy( );

            Duration intervalo = lembrete.getRecorrencia( ).montarIntevalo( );
            if( intervalo != null && !intervalo.isZero( ) && !intervalo.isNegative( ) )
                return new IntervaloStrategy( );
        }

        return null;
    }
}
