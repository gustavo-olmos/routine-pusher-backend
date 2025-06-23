package com.routine.pusher.core.strategy;

import java.time.LocalDateTime;

public interface ProximaNotificacaoStrategy<T>
{
    LocalDateTime calcular( T t );
}
