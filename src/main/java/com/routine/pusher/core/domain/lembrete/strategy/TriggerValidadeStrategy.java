package com.routine.pusher.core.domain.lembrete.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.strategy.TriggerStrategy;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

public class TriggerValidadeStrategy implements TriggerStrategy<Lembrete>
{
    @Override
    public Trigger criarTrigger( Lembrete lembrete )
    {
        LocalDateTime validade = lembrete.getDataFim( );

        String cronExpression = lembrete.montarCronExpression( );
        if( !Objects.equals( cronExpression, "" ) ) {
            Date dataFim = Date.from( validade.atZone( ZoneId.systemDefault( ) ).toInstant( ) );
            return TriggerBuilder.newTrigger( )
                    .withIdentity( lembrete.getId( ).toString( ) )
                    .withSchedule( CronScheduleBuilder.cronSchedule( cronExpression ) )
                    .endAt( dataFim )
                    .build( );
        }

        LocalDateTime momento = lembrete.calcularProximaNotificacao( );
        if( Objects.nonNull( momento ) && momento.isBefore( validade ) ) {
            Date dataInicio = Date.from( momento.atZone( ZoneId.systemDefault( ) ).toInstant( ) );
            return TriggerBuilder.newTrigger( )
                    .withIdentity( lembrete.getId( ).toString( ) )
                    .startAt( dataInicio )
                    .build( );
        }

        throw new RuntimeException("Não foi possível agendar o lembrete");
    }
}
