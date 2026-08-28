package com.routine.pusher.application.job;

import com.routine.pusher.application.usecase.SessaoUseCase;
import jakarta.inject.Inject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

/**
 * Gatilho da faxina de sessões expiradas — a regra mora no caso de uso; aqui é só o relógio.
 * Agendado de forma idempotente no boot, ver {@code SessaoConfig}.
 */
public class SessaoExpiradaJob implements Job
{
    @Inject
    private SessaoUseCase useCase;

    @Override
    public void execute( JobExecutionContext context )
    {
        useCase.removerExpiradas( );
    }
}
