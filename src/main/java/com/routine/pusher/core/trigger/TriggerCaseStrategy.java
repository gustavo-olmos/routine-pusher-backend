package com.routine.pusher.core.trigger;

import org.quartz.Trigger;

public interface TriggerCaseStrategy<T>
{
    Trigger criarTrigger( T t );
}
