package com.routine.pusher.core.trigger.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.trigger.TriggerCaseStrategy;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

public class TriggerQuantidadeStrategy implements TriggerCaseStrategy<Lembrete>
{
    @Override
    public Trigger criarTrigger( Lembrete lembrete )
    {
        Trigger trigger = null;

        Recorrencia recorrencia = lembrete.getRecorrencia( );
        String cronExpression = recorrencia.montarCronExpression( lembrete.getNotificacao( ) );
        if( !Objects.equals( cronExpression, "" ) ) {
            trigger = TriggerBuilder.newTrigger( )
                     .withIdentity( lembrete.getId( ).toString( ) )
                     .withSchedule( CronScheduleBuilder.cronSchedule( cronExpression ) )
                     .build( );
        }

        Notificacao notificacao = lembrete.getNotificacao( );
        LocalDateTime momento = notificacao.calcularProximaNotificacao( lembrete );
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
