package com.routine.pusher.infrastructure.common.helper.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

public class TriggerRecorrenciaQuantidadeStrategy implements TriggerStrategy
{
    @Override
    public Trigger criarTrigger( Lembrete lembrete )
    {
        Trigger trigger = null;
        Recorrencia recorrencia = lembrete.getRecorrencia( );

        String cronExpression = lembrete.montaCronExpression( );
        if( !Objects.equals( cronExpression, "" ) ) {
             trigger = TriggerBuilder.newTrigger( )
                     .withIdentity( lembrete.getId( ).toString( ) )
                     .withSchedule( CronScheduleBuilder.cronSchedule( cronExpression ) )
                     .build( );
        }

        LocalDateTime momento = lembrete.calcularProxNotificacaoComIntervalo( );
        if( Objects.nonNull( momento ) ) {
            Date dataInicio = Date.from( momento.atZone( ZoneId.systemDefault( ) ).toInstant( ) );
            trigger = TriggerBuilder.newTrigger( )
                    .withIdentity( lembrete.getId( ).toString( ) )
                    .startAt( dataInicio )
                    .build( );
        }

        recorrencia.setQuantidade( recorrencia.getQuantidade( ) - 1 );
        return trigger;
    }
}
