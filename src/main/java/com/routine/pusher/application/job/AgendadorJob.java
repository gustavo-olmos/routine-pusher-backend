package com.routine.pusher.application.job;

import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import org.quartz.Job;
import org.quartz.Scheduler;

public interface AgendadorJob extends Job
{
    void reagendar( Scheduler scheduler, LembreteOutputDTO dto );
}
