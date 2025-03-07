package com.routine.pusher.service.implementation;

import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.service.interfaces.AgendadorService;
import com.routine.pusher.util.AgendadorJobUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AgendadorServiceImpl implements AgendadorService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AgendadorServiceImpl.class);

    private final Scheduler scheduler;

    public AgendadorServiceImpl(Scheduler scheduler )
    {
        this.scheduler = scheduler;
    }


    @Override
    public void agendar( LembreteDTO dto )
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

    //TODO: Verificar necessidade desse método
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

    //TODO: Verificar necessidade desse método
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
