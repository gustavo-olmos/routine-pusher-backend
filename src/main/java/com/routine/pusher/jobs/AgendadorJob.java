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
        // Obtendo os detalhes do job (o lembrete)
        JobDataMap jobDataMap = context.getJobDetail( ).getJobDataMap( );

        //TODO: verificar se futuramente irei utilizar o dto que vêm daqui
        LembreteDTO dto = ( LembreteDTO ) jobDataMap.get( AgendadorServiceImpl.class.getSimpleName( ) );
        LOGGER.info("Intervalo de notificação {}", dto.getIntervalo( ));

        // Envia para a fila do RabbitMQ Producer
        producer.sendMessage( dto );
    }
}
