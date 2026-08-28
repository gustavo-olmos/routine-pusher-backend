package com.routine.pusher.core.trigger;

import com.routine.pusher.core.domain.feriado.port.FeriadoPort;
import org.quartz.Trigger;

public interface TriggerCaseStrategy<T>
{
    /**
     * {@code feriados} chega até aqui porque o disparo real precisa obedecer à mesma política de
     * calendário que a projeção exibe. Se o trigger ignorasse o feriado, a API mostraria uma data e
     * a notificação chegaria em outra.
     */
    Trigger criarTrigger( T t, FeriadoPort feriados );
}
