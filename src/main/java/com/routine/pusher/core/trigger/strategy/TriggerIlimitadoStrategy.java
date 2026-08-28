package com.routine.pusher.core.trigger.strategy;

import com.routine.pusher.core.domain.feriado.port.FeriadoPort;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.trigger.TriggerCaseStrategy;
import com.routine.pusher.infrastructure.exceptions.StrategyException;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

public class TriggerIlimitadoStrategy implements TriggerCaseStrategy<Lembrete>
{
    @Override
    public Trigger criarTrigger( Lembrete lembrete, FeriadoPort feriados )
    {
        Recorrencia recorrencia = lembrete.getRecorrencia( );
        String cronExpression = recorrencia.montarCronExpression( lembrete.getNotificacao( ) );

        // Com política de dia útil o trigger não pode ser um cron permanente: o Quartz dispararia no
        // feriado, porque a expressão só conhece dia da semana. Cai no caminho de disparo único mais
        // reagendamento — o mesmo que o intervalo já usa — para que a regra de calendário fique num
        // lugar só, o domínio, e não duplicada dentro do agendador.
        if( !Objects.equals( cronExpression, "" ) && !recorrencia.exigeDiaUtil( ) ) {
            return TriggerBuilder.newTrigger( )
                    .withIdentity( lembrete.getId( ).toString( ) )
                    .withSchedule( CronScheduleBuilder.cronSchedule( cronExpression ) )
                    .build( );
        }

        Notificacao notificacao = lembrete.getNotificacao( );
        LocalDateTime momento = notificacao.calcularProximaNotificacao( lembrete, feriados );
        if( Objects.nonNull( momento ) ) {
            Date dataInicio = Date.from( momento.atZone( ZoneId.systemDefault( ) ).toInstant( ) );
            return TriggerBuilder.newTrigger( )
                    .withIdentity( lembrete.getId( ).toString( ) )
                    .startAt( dataInicio )
                    .build( );
        }

        throw new StrategyException("Não foi possível agendar o lembrete");
    }
}
