package com.routine.pusher.core.domain.recorrencia.strategy;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.strategy.TriggerStrategy;
import org.quartz.Trigger;

public class TriggerIlimitadoStrategy implements TriggerStrategy<Lembrete>
{
    @Override
    public Trigger criarTrigger( Lembrete lembrete )
    {
        return null;
    }
}
