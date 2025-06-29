package com.routine.pusher.core.domain.lembrete.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.trigger.TriggerCaseStrategy;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TriggerDatasEspecificasStrategy implements TriggerCaseStrategy<Lembrete>
{
    @Override
    public Trigger criarTrigger( Lembrete lembrete )
    {
        //TODO: Essa strategy deve ser movida para notificacao
        Notificacao notificacao = lembrete.getNotificacao( );
        LocalDateTime proximaNotificacao = notificacao.getDatasEspecificadas( ).get( 0 );
        notificacao.getDatasEspecificadas( ).remove( 0 );

        Date dataExecucao = Date.from( proximaNotificacao.atZone( ZoneId.systemDefault( ) ).toInstant( ) );

        return TriggerBuilder.newTrigger( )
                .withIdentity( lembrete.getId( ).toString( ) )
                .startAt( dataExecucao )
                .build( );
    }
}
