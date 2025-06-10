package com.routine.pusher.core.domain.lembrete.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.strategy.TriggerStrategy;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TriggerDatasEspecificasStrategy implements TriggerStrategy<Lembrete>
{
    @Override
    public Trigger criarTrigger( Lembrete lembrete )
    {
        LocalDateTime proximaNotificacao = lembrete.getDatasEspecificadas( ).get( 0 );
        lembrete.getDatasEspecificadas( ).remove( 0 );

        Date dataExecucao = Date.from( proximaNotificacao.atZone( ZoneId.systemDefault( ) ).toInstant( ) );

        return TriggerBuilder.newTrigger( )
                .withIdentity( lembrete.getId( ).toString( ) )
                .startAt( dataExecucao )
                .build( );
    }
}
