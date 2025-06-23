package com.routine.pusher.core.strategy.factory;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.strategy.TriggerDatasEspecificasStrategy;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.domain.recorrencia.strategy.TriggerIlimitadoStrategy;
import com.routine.pusher.core.domain.recorrencia.strategy.TriggerQuantidadeStrategy;
import com.routine.pusher.core.domain.lembrete.strategy.TriggerValidadeStrategy;
import com.routine.pusher.core.strategy.TriggerStrategy;

import java.util.Objects;

public class TriggerStrategyFactory
{
    public static TriggerStrategy<Lembrete> getStrategy( Lembrete lembrete )
    {
        Notificacao notificacao = lembrete.getNotificacao( );
        if( notificacao.getDatasEspecificadas( ) != null )
            return new TriggerDatasEspecificasStrategy( );

        if( Objects.nonNull( notificacao.getDataFim( ) ) ) {
            return new TriggerValidadeStrategy( );
        } else {
            Recorrencia recorrencia = lembrete.getRecorrencia( );
            if( Objects.nonNull( recorrencia ) && recorrencia.getQuantidade( ) > 0 )
                return new TriggerQuantidadeStrategy( );

            return new TriggerIlimitadoStrategy( );
        }
    }
}
