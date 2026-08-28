package com.routine.pusher.core.domain.notificacao.strategy;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificacaoCaseStrategy<T>
{
    LocalDateTime calcularProximaNotificacao( T t );

    /**
     * Projeta as próximas {@code limite} ocorrências, em ordem crescente e sempre no futuro.
     * <p>
     * É uma leitura: diferente de {@link #calcularProximaNotificacao}, roda a cada serialização de
     * saída e por isso não pode alterar o lembrete. Aqui mora apenas o "quando dispara" da
     * recorrência — validade e cota são limites de negócio aplicados por
     * {@code Notificacao.calcularProximasNotificacoes}.
     */
    List<LocalDateTime> calcularProximasNotificacoes( T t, int limite );
}
