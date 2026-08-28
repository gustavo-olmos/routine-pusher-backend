package com.routine.pusher.core.trigger.strategy;

import com.routine.pusher.core.domain.feriado.port.FeriadoPort;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.trigger.TriggerCaseStrategy;
import com.routine.pusher.infrastructure.exceptions.StrategyException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;

public class TriggerDatasEspecificasStrategy implements TriggerCaseStrategy<Lembrete>
{
    @Override
    public Trigger criarTrigger( Lembrete lembrete, FeriadoPort feriados )
    {
        Notificacao notificacao = lembrete.getNotificacao( );

        // Escolhe a menor data ainda futura sem mutar a lista: o job recarrega o lembrete do banco a
        // cada disparo, então remover elementos em memória não persistiria e reagendaria sempre a
        // data mais antiga (já vencida), disparando em rajada.
        LocalDateTime proximaNotificacao = notificacao.getDatasEspecificadas( ).stream( )
                .filter( data -> data.isAfter( LocalDateTime.now( ) ) )
                .min( Comparator.naturalOrder( ) )
                .orElseThrow( () -> new StrategyException( "Não há datas futuras para agendar" ) );

        Date dataExecucao = Date.from( proximaNotificacao.atZone( ZoneId.systemDefault( ) ).toInstant( ) );

        return TriggerBuilder.newTrigger( )
                .withIdentity( lembrete.getId( ).toString( ) )
                .startAt( dataExecucao )
                .build( );
    }
}
