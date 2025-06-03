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

public class TriggerRecorrenciaValidadeStrategy implements TriggerStrategy
{
    @Override
    public Trigger criarTrigger( Lembrete lembrete )
    {
        Recorrencia recorrencia = lembrete.getRecorrencia( );
        LocalDateTime validade = recorrencia.getValidade( );

        String cronExpression = lembrete.montaCronExpression( );
        if( Objects.equals( cronExpression, "" ) ) {
            Date dataFim = Date.from( validade.atZone( ZoneId.systemDefault( ) ).toInstant( ) );
            return TriggerBuilder.newTrigger( )
                    .withIdentity( lembrete.getId( ).toString( ) )
                    .withSchedule( CronScheduleBuilder.cronSchedule( lembrete.montaCronExpression( ) ) )
                    .endAt( dataFim )
                    .build( );
        }

        LocalDateTime momento = lembrete.calcularProxNotificacaoComIntervalo( );
        if( Objects.nonNull( momento ) && momento.isBefore( validade ) ) {
            Date dataInicio = Date.from( momento.atZone( ZoneId.systemDefault( ) ).toInstant( ) );
            return TriggerBuilder.newTrigger( )
                    .withIdentity( lembrete.getId( ).toString( ) )
                    .startAt( dataInicio )
                    .build( );
        }

        throw new RuntimeException("Po meu parceiro, ramelou também");
    }
}
