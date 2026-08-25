package com.routine.pusher.core.trigger.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.trigger.TriggerCaseStrategy;
import com.routine.pusher.infrastructure.exceptions.StrategyException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Agenda o próximo disparo de um lembrete limitado por quantidade. O limite não é imposto aqui: cada
 * trigger é um disparo único posicionado na próxima notificação (calculada pela recorrência cron ou
 * por intervalo). Quem decrementa e persiste a contagem — e para de reagendar quando ela esgota — é
 * o ciclo de vida do disparo ({@code LembreteExecutorJob} + {@code Recorrencia.consumirQuantidade}).
 */
public class TriggerQuantidadeStrategy implements TriggerCaseStrategy<Lembrete>
{
    @Override
    public Trigger criarTrigger( Lembrete lembrete )
    {
        Notificacao notificacao = lembrete.getNotificacao( );
        LocalDateTime momento = notificacao.calcularProximaNotificacao( lembrete );
        if( momento == null )
            throw new StrategyException( "Não foi possível calcular a próxima notificação por quantidade" );

        Date dataInicio = Date.from( momento.atZone( ZoneId.systemDefault( ) ).toInstant( ) );

        return TriggerBuilder.newTrigger( )
                .withIdentity( lembrete.getId( ).toString( ) )
                .startAt( dataInicio )
                .build( );
    }
}
