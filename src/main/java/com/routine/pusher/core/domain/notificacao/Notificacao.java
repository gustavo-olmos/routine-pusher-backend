package com.routine.pusher.core.domain.notificacao;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.notificacao.strategy.NotificacaoCaseStrategy;
import com.routine.pusher.core.domain.notificacao.factory.NotificacaoStrategyFactory;
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
}
