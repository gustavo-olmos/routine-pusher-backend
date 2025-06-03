package com.routine.pusher.infrastructure.common.helper.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;

import java.time.LocalDateTime;
import java.util.Objects;

public class TriggerStrategyFactory
{
    public static TriggerStrategy getStrategy( Lembrete lembrete )
    {
        if ( !lembrete.getMomentosEspecificados( ).isEmpty( ) )
            return new TriggerDatasEspecificasStrategy( );

        Recorrencia recorrencia = lembrete.getRecorrencia( );
        if ( Objects.nonNull( recorrencia ) ) {

            LocalDateTime validade = recorrencia.getValidade();
            if ( Objects.nonNull( validade ) )
                return new TriggerRecorrenciaValidadeStrategy( );

            if ( recorrencia.getQuantidade( ) > 0 )
                return new TriggerRecorrenciaQuantidadeStrategy( );
        }

        throw new IllegalArgumentException("Não há notificações disponíveis para esse lembrete");
    }
}
