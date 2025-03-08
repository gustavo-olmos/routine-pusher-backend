package com.routine.pusher.jobs;

import com.routine.pusher.event.RabbitMQProducer;
import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.service.implementation.AgendadorServiceImpl;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class AgendadorJob implements Job
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AgendadorJob.class);

    private final RabbitMQProducer producer;

    public AgendadorJob( RabbitMQProducer producer )
    {
        this.producer = producer;
    }

    @Override
    public void execute( JobExecutionContext context ) {
        JobDataMap jobDataMap = context.getJobDetail( ).getJobDataMap( );

        //TODO: verificar como pegar somente o id de um dto
        LembreteDTO dto = ( LembreteDTO ) jobDataMap.get( LembreteDTO.class.getSimpleName( ) );
        LOGGER.info("Intervalo de notificação {}", dto.getIntervalo( ));

        producer.sendMessage( dto );
    }
}
