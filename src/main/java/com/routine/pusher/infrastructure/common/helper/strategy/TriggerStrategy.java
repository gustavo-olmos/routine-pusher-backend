package com.routine.pusher.infrastructure.common.helper.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import org.quartz.Trigger;

public interface TriggerStrategy
{
    Trigger criarTrigger( Lembrete lembrete );
}
