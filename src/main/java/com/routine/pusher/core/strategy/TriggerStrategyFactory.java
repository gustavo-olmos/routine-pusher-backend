package com.routine.pusher.core.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.strategy.TriggerDatasEspecificasStrategy;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.domain.recorrencia.strategy.TriggerIlimitadoStrategy;
import com.routine.pusher.core.domain.recorrencia.strategy.TriggerQuantidadeStrategy;
import com.routine.pusher.core.domain.lembrete.strategy.TriggerValidadeStrategy;

import java.util.Objects;

public class TriggerStrategyFactory
{
    public static TriggerStrategy<Lembrete> getStrategy( Lembrete lembrete )
    {
        if( !lembrete.getDatasEspecificadas( ).isEmpty( ) )
            return new TriggerDatasEspecificasStrategy( );

        if( Objects.nonNull( lembrete.getDataFim( ) ) ) {
            return new TriggerValidadeStrategy( );
        } else {
            Recorrencia recorrencia = lembrete.getRecorrencia( );
            if( Objects.nonNull( recorrencia ) && recorrencia.getQuantidade( ) > 0 )
                return new TriggerQuantidadeStrategy( );

            return new TriggerIlimitadoStrategy( );
        }

        throw new IllegalArgumentException("Não há notificações disponíveis para esse lembrete");
    }
}
