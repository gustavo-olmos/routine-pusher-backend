package com.routine.pusher.core.trigger.factory;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.strategy.TriggerDatasEspecificasStrategy;
import com.routine.pusher.core.trigger.strategy.TriggerValidadeStrategy;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.trigger.strategy.TriggerIlimitadoStrategy;
import com.routine.pusher.core.trigger.strategy.TriggerQuantidadeStrategy;
import com.routine.pusher.core.trigger.TriggerCaseStrategy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class TriggerStrategyFactory
{
    public static TriggerCaseStrategy<Lembrete> getStrategy(Lembrete lembrete )
    {
        Notificacao notificacao = lembrete.getNotificacao( );
        List<LocalDateTime> datasEspecificadas = notificacao.getDatasEspecificadas();
        if( datasEspecificadas != null && !datasEspecificadas.isEmpty( ) )
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
