package com.routine.pusher.core.domain.notificacao;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.notificacao.strategy.NotificacaoCaseStrategy;
import com.routine.pusher.core.domain.notificacao.factory.NotificacaoStrategyFactory;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacao
{
    private Long id;
    private List<String> metodo;
    private LocalTime horario;
    private LocalDateTime proximaExecucao;
    private LocalDateTime ultimaExecucao;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private List<LocalDateTime> datasEspecificadas;


    public boolean aindaTemNotificacao( Lembrete lembrete )
    {
        return Objects.nonNull( this.calcularProximaNotificacao( lembrete ) );
    }

    public LocalDateTime calcularProximaNotificacao( Lembrete lembrete )
    {
        NotificacaoCaseStrategy<Lembrete> caseStrategy = NotificacaoStrategyFactory.getStrategy( lembrete );
        return caseStrategy.calcularProximaNotificacao( lembrete );
    }

    /**
     * Projeta as próximas execuções para leitura, sem alterar o lembrete. A recorrência responde
     * "quando dispara"; os limites de negócio são aplicados aqui, porque não pertencem a nenhuma
     * strategy isolada: a validade encerra a série numa data e a cota a encerra numa contagem.
     * <p>
     * É uma prévia, não uma promessa — o agendamento real continua sendo um disparo de cada vez, e
     * uma conclusão ou alteração do lembrete refaz a série.
     */
    public List<LocalDateTime> calcularProximasNotificacoes( Lembrete lembrete, int limite )
    {
        NotificacaoCaseStrategy<Lembrete> caseStrategy = NotificacaoStrategyFactory.getStrategy( lembrete );
        if( caseStrategy == null || limite <= 0 ) return List.of( );

        return caseStrategy.calcularProximasNotificacoes( lembrete, limite ).stream( )
                .filter( data -> dataFim == null || !data.isAfter( dataFim ) )
                .limit( cotaRestante( lembrete, limite ) )
                .toList( );
    }

    private long cotaRestante( Lembrete lembrete, int limite )
    {
        Recorrencia recorrencia = lembrete.getRecorrencia( );
        if( recorrencia == null || recorrencia.getQuantidade( ) == null ) return limite;

        return Math.min( limite, Math.max( 0, recorrencia.getQuantidade( ) ) );
    }
}
