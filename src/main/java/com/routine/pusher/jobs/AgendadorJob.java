package com.routine.pusher.jobs;

import com.routine.pusher.event.RabbitMQProducer;
import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.service.implementation.AgendadorServiceImpl;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;


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
    public void execute( JobExecutionContext context )
    {
        JobDataMap jobDataMap = context.getJobDetail( ).getJobDataMap( );
        String jobId = context.getJobDetail( ).getKey( ).getName( );

        LembreteDTO dto = ( LembreteDTO ) jobDataMap.get( jobId );

        if (dto == null) {
            LOGGER.warn("LembreteDTO não encontrado para o job ID: {}", jobId);
            return;
        }

        List<LocalDateTime> notificacoesAgendadas = dto.getMomentoNotificacao( );
        if( notificacoesAgendadas.isEmpty( ) ) {
            LOGGER.info("Nenhuma notificação pendente para o job {}", jobId);
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime proximaNotificacao = notificacoesAgendadas.get(0);

        if( proximaNotificacao.isBefore( agora ) || proximaNotificacao.isEqual( agora ) ) {
            LOGGER.info("Notificando job com id: {}", jobId);
            producer.sendMessage(dto);
        }

        if(!notificacoesAgendadas.isEmpty()) {
            reagendarProximaNotificacao(context.getScheduler(), dto);
        } else {
            try {
                context.getScheduler( ).deleteJob( context.getJobDetail( ).getKey( ) );
            }
            catch ( SchedulerException ex ) {
                LOGGER.error("Erro ao remover job {}", ex.getMessage( ));
                throw new RuntimeException();
            }
        }
    }

    private void reagendarProximaNotificacao( Scheduler scheduler, LembreteDTO dto )
    {
        List<LocalDateTime> notificacoesAgendadas = dto.getMomentoNotificacao( );

        if ( notificacoesAgendadas.isEmpty( ) ) return;

        LocalDateTime proximaNotificacao = notificacoesAgendadas.get( 0 );
        Date proximaExecucao = Date.from( proximaNotificacao.atZone( ZoneId.systemDefault( ) ).toInstant( ) );

        try {
            Trigger novoTrigger = TriggerBuilder.newTrigger( )
                    .withIdentity( dto.getId( ).toString( ) )
                    .startAt( proximaExecucao )
                    .build( );

            scheduler.rescheduleJob( new TriggerKey( dto.getId( ).toString( ) ), novoTrigger );
            LOGGER.info( "Reagendado job {} para {}", dto.getId( ), proximaExecucao );
        }
        catch ( SchedulerException e ) {
            LOGGER.error("Erro ao reagendar job {}", dto.getId( ), e);
        }
    }
}
