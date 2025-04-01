package com.routine.pusher.application.service;

import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import org.quartz.Scheduler;
import org.quartz.Trigger;

public interface AgendadorService
{
    void agendar( LembreteOutputDTO dto );

    void reagendar( Scheduler scheduler, String key, Trigger novoTrigger );
}
