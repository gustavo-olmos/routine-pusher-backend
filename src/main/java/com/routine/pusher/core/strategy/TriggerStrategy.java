package com.routine.pusher.core.strategy;

import org.quartz.Trigger;

public interface TriggerStrategy<T>
{
    Trigger criarTrigger( T t );
}
