package com.routine.pusher.application.service;

import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.infrastructure.common.util.AgendadorJobUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AgendadorServiceImpl implements AgendadorService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AgendadorServiceImpl.class);

    private final Scheduler scheduler;

    public AgendadorServiceImpl( Scheduler scheduler )
    {
        this.scheduler = scheduler;
    }


    @Override
    public void agendar( LembreteOutputDTO dto )
    {
        JobDetail jobDetail = AgendadorJobUtil.montarNovoJob( dto );
        Trigger trigger = AgendadorJobUtil.montarNovoTrigger( dto );

        try {
            scheduler.scheduleJob( jobDetail, trigger );
        }
        catch ( SchedulerException e ) {
            LOGGER.error(e.getMessage( ), e);
        }
    }

    @PostConstruct
    public void init()
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
