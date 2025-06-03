package com.routine.pusher.infrastructure.common.helper.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TriggerDatasEspecificasStrategy implements TriggerStrategy
{
    @Override
    public Trigger criarTrigger( Lembrete lembrete )
    {
        LocalDateTime proximaNotificacao = lembrete.getMomentosEspecificados( ).get( 0 );
        lembrete.getMomentosEspecificados( ).remove( 0 );

        Date dataExecucao = Date.from( proximaNotificacao.atZone( ZoneId.systemDefault( ) ).toInstant( ) );

        return TriggerBuilder.newTrigger( )
                .withIdentity( lembrete.getId( ).toString( ) )
                .startAt( dataExecucao )
                .build( );
    }
}
