package com.routine.pusher.core.strategy;

import org.quartz.Trigger;

public interface TriggerStrategy<T>
{
    //TODO: atualmente essa strategy está sendo usada somente para lembrete,
    // precisa refatorar as implementacoes para usar de maneira mais generica
    Trigger criarTrigger( T t );
}
