package com.routine.pusher.core.domain.notificacao.strategy;

import java.time.LocalDateTime;

public interface NotificacaoCaseStrategy<T>
{
    LocalDateTime calcularProximaNotificacao( T t );
}
