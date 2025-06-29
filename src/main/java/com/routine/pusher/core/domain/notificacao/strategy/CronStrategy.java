package com.routine.pusher.core.domain.notificacao.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.infrastructure.exceptions.ProcessoException;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

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

            return proxima.toInstant( ).atZone( ZoneId.systemDefault( ) ).toLocalDateTime( );

        } catch ( ParseException e ) {
            throw new ProcessoException( "Cálculo de próxima notificação", e.getMessage() );
        }
    }
}
