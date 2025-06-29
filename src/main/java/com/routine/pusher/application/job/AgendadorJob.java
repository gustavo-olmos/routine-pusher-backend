package com.routine.pusher.application.job;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.infrastructure.common.scheduler.QuartzScheduler;
import com.routine.pusher.infrastructure.exceptions.ProcessoException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AgendadorJob
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AgendadorJob.class);
    private static Scheduler scheduler = initializeScheduler( );

    private AgendadorJob( Scheduler scheduler )
    {
        AgendadorJob.scheduler = scheduler;
    }


    private static Scheduler initializeScheduler( )
    {
        try {
            return new StdSchedulerFactory( ).getScheduler( );
        }
        catch ( SchedulerException ex ) {
            throw new ProcessoException( "Inicialização de Scheduler", ex.getMessage( ) );
        }
    }

    public static void agendar( Lembrete lembrete )
    {
        QuartzScheduler<Lembrete> quartz = new QuartzScheduler<>( );
        JobDetail jobDetail = quartz.criarJob( lembrete, lembrete.getId( ).toString( ) );
        Trigger trigger = quartz.criarTrigger( lembrete );

        try {
            scheduler.scheduleJob( jobDetail, trigger );
        }
        catch ( SchedulerException e ) {
            LOGGER.error(e.getMessage( ), e);
        }
    }

    public static void reagendar( JobExecutionContext context, String key, Trigger novoTrigger )
    {
        try {
            context.getScheduler( ).rescheduleJob( new TriggerKey( key ), novoTrigger );

            //TODO: VERIFICAR PROXIMA EXECUCAO CORRETAMENTE PARA MELHORAR O LOG
            LOGGER.info("Reagendando job de id {} para {}", key, novoTrigger.getNextFireTime( ) );
        }
        catch ( SchedulerException e ) {
            LOGGER.error("Erro ao reagendar job {}", key, e);
        }
    }

    @PostConstruct
    public void init( )
    {
        try {
            scheduler.start( );
        }
        catch ( SchedulerException e ) {
            LOGGER.error(e.getMessage( ), e);
        }
    }

    @PreDestroy
    public void preDestroy( )
    {
       try {
           scheduler.shutdown( );
       }
       catch ( SchedulerException e ) {
           LOGGER.error(e.getMessage( ), e);
       }
    }

}
