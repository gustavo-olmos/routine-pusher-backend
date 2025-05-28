package com.routine.pusher.application.job;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.infrastructure.common.helper.AgendadorJobBuilder;
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
        try { return new StdSchedulerFactory( ).getScheduler( ); }
        catch (SchedulerException e) { throw new RuntimeException(e); }
    }

    public static void agendar( Lembrete lembrete )
    {
        JobDetail jobDetail = AgendadorJobBuilder.montarNovoJob( lembrete );
        Trigger trigger = AgendadorJobBuilder.montarNovoTrigger( lembrete );

        try {
            scheduler.scheduleJob( jobDetail, trigger );
        }
        catch ( SchedulerException e ) {
            LOGGER.error(e.getMessage( ), e);
        }
    }

    public static void reagendar( Scheduler scheduler, String key, Trigger novoTrigger )
    {
        try {
            scheduler.rescheduleJob( new TriggerKey( key ), novoTrigger );
            LOGGER.info("Reagendado job para {}", key);
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
