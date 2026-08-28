package com.routine.pusher.core.domain.notificacao.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IntervaloStrategy implements NotificacaoCaseStrategy<Lembrete>
{
    @Override
    public LocalDateTime calcularProximaNotificacao(Lembrete lembrete )
    {
        Recorrencia recorrencia = lembrete.getRecorrencia( );
        Notificacao notificacao = lembrete.getNotificacao( );
        if( recorrencia == null || notificacao == null ) return null;

        Duration intervalo = recorrencia.montarIntevalo( );
        LocalDateTime base = ( notificacao.getUltimaExecucao( ) != null )
                ? notificacao.getUltimaExecucao( )
                : notificacao.getDataInicio( );

        return base.plus( intervalo );
    }

    @Override
    public List<LocalDateTime> calcularProximasNotificacoes( Lembrete lembrete, int limite )
    {
        Recorrencia recorrencia = lembrete.getRecorrencia( );
        Notificacao notificacao = lembrete.getNotificacao( );
        if( recorrencia == null || notificacao == null ) return List.of( );

        Duration intervalo = recorrencia.montarIntevalo( );
        if( intervalo.isZero( ) || intervalo.isNegative( ) ) return List.of( );

        LocalDateTime base = ( notificacao.getUltimaExecucao( ) != null )
                ? notificacao.getUltimaExecucao( )
                : notificacao.getDataInicio( );

        if( base == null ) return List.of( );

        List<LocalDateTime> proximas = new ArrayList<>( );

        LocalDateTime cursor = primeiraOcorrenciaFutura( base, intervalo );
        while( proximas.size( ) < limite ) {
            proximas.add( cursor );
            cursor = cursor.plus( intervalo );
        }

        return proximas;
    }

    /**
     * A base é o último disparo (ou o início, se ainda não houve nenhum) e pode estar bem no passado
     * — aplicação fora do ar, lembrete antigo. Projetar a partir dela renderia ocorrências vencidas,
     * então salta de uma vez os intervalos já decorridos em vez de somar um a um.
     */
    private LocalDateTime primeiraOcorrenciaFutura( LocalDateTime base, Duration intervalo )
    {
        long decorridos = Duration.between( base, LocalDateTime.now( ) ).dividedBy( intervalo );

        return base.plus( intervalo.multipliedBy( Math.max( 0, decorridos ) + 1 ) );
    }
}
