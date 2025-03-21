package com.routine.pusher.application.job;

import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.infrastructure.message.RabbitMQProducer;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;


@Component
public class AgendadorJobImpl implements AgendadorJob
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AgendadorJobImpl.class);

    private final RabbitMQProducer producer;

    public AgendadorJobImpl(RabbitMQProducer producer )
    {
        this.producer = producer;
    }

    @Override
    public void execute( JobExecutionContext context )
    {
        JobDataMap jobDataMap = context.getJobDetail( ).getJobDataMap( );
        String jobId = context.getJobDetail( ).getKey( ).getName( );

        LembreteOutputDTO dto = (LembreteOutputDTO) jobDataMap.get( jobId );

        if (dto == null) {
            LOGGER.warn("LembreteDTO não encontrado para o job ID: {}", jobId);
            return;
        }

        //TODO: CORRIGIR ISSO
        List<LocalDateTime> notificacoesAgendadas = dto.datasEspecificas( );
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
            reagendar(context.getScheduler(), dto);
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

    @Override
    public void reagendar( Scheduler scheduler, LembreteOutputDTO dto )
    {
        List<LocalDateTime> notificacoesAgendadas = dto.datasEspecificas( );

        if ( notificacoesAgendadas.isEmpty( ) ) return;

        LocalDateTime proximaNotificacao = notificacoesAgendadas.get( 0 );
        Date proximaExecucao = Date.from( proximaNotificacao.atZone( ZoneId.systemDefault( ) ).toInstant( ) );

        try {
            Trigger novoTrigger = TriggerBuilder.newTrigger( )
                    .withIdentity( dto.id( ).toString( ) )
                    .startAt( proximaExecucao )
                    .build( );

            scheduler.rescheduleJob( new TriggerKey( dto.id( ).toString( ) ), novoTrigger );
            LOGGER.info( "Reagendado job {} para {}", dto.id( ), proximaExecucao );
        }
        catch ( SchedulerException e ) {
            LOGGER.error("Erro ao reagendar job {}", dto.id( ), e);
        }
    }
}
