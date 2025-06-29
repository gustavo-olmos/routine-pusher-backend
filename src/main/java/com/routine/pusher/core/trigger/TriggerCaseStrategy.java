package com.routine.pusher.core.trigger;

import org.quartz.Trigger;

public interface TriggerCaseStrategy<T>
{
    //TODO: atualmente essa strategy está sendo usada somente para lembrete,
    // precisa refatorar as implementacoes para usar de maneira mais generica
    Trigger criarTrigger( T t );
}
