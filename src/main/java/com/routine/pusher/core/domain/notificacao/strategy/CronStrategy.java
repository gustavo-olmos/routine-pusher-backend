package com.routine.pusher.core.domain.notificacao.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.infrastructure.exceptions.ProcessoException;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CronStrategy implements NotificacaoCaseStrategy<Lembrete>
{
    @Override
    public LocalDateTime calcularProximaNotificacao(Lembrete lembrete )
    {
        Recorrencia recorrencia = lembrete.getRecorrencia( );
        Notificacao notificacao = lembrete.getNotificacao( );
        if( recorrencia == null || notificacao == null ) return null;

        try {
            String cron = recorrencia.montarCronExpression( notificacao );
            CronExpression cronExpr = new CronExpression( cron );
            Date proxima = cronExpr.getNextValidTimeAfter( new Date( ) );

            return converterParaLocal( proxima );

        } catch ( ParseException e ) {
            throw new ProcessoException( "Cálculo de próxima notificação", e.getMessage() );
        }
    }

    @Override
    public List<LocalDateTime> calcularProximasNotificacoes( Lembrete lembrete, int limite )
    {
        Recorrencia recorrencia = lembrete.getRecorrencia( );
        Notificacao notificacao = lembrete.getNotificacao( );
        if( recorrencia == null || notificacao == null ) return List.of( );

        try {
            String cron = recorrencia.montarCronExpression( notificacao );
            CronExpression cronExpr = new CronExpression( cron );

            List<LocalDateTime> proximas = new ArrayList<>( );

            // Cada data encontrada vira o ponto de partida da busca seguinte: é assim que o Quartz
            // percorre a expressão, um disparo de cada vez.
            Date cursor = new Date( );
            while( proximas.size( ) < limite ) {
                Date proxima = cronExpr.getNextValidTimeAfter( cursor );
                if( proxima == null ) break;

                proximas.add( converterParaLocal( proxima ) );
                cursor = proxima;
            }

            return proximas;

        } catch ( ParseException e ) {
            throw new ProcessoException( "Cálculo das próximas notificações", e.getMessage() );
        }
    }

    private LocalDateTime converterParaLocal( Date data )
    {
        return data.toInstant( ).atZone( ZoneId.systemDefault( ) ).toLocalDateTime( );
    }
}
