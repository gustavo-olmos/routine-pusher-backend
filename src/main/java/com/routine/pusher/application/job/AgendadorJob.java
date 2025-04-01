package com.routine.pusher.application.job;

import com.routine.pusher.application.service.NotificadorSSEService;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.data.model.dto.RecorrenciaDTOOutput;
import com.routine.pusher.infrastructure.message.RabbitMQProducer;
import lombok.AllArgsConstructor;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

@Component
@AllArgsConstructor
public class AgendadorJob implements Job
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AgendadorJob.class);

    private final RabbitMQProducer producer;
    private final NotificadorSSEService service;

    @Override
    public void execute( JobExecutionContext executionContext )
    {
        JobDataMap jobDataMap = executionContext.getJobDetail( ).getJobDataMap( );
        String jobId = executionContext.getJobDetail( ).getKey( ).getName( );

        LembreteOutputDTO dto = (LembreteOutputDTO) jobDataMap.get( jobId );
        if( dto == null ) {
            LOGGER.warn("LembreteDTO não encontrado para o job ID: {}", jobId);
            return;
        }

        List<LocalDateTime> notificacoesAgendadas = dto.datasEspecificas( );
        if( notificacoesAgendadas.isEmpty( ) ) {
            try {
                LOGGER.info("Nenhuma notificação pendente para o job {}", jobId);
                executionContext.getScheduler().deleteJob(executionContext.getJobDetail().getKey());
            } catch (SchedulerException ex) {
                LOGGER.error("Erro ao remover job {}", ex.getMessage());
                throw new RuntimeException();
            }
        }

        notificar( notificacoesAgendadas.get( 0 ), jobId, dto.titulo( ) );
        if ( dto.recorrencia( ) == null ) {
            reagendarComDataEspecifica( executionContext.getScheduler( ), dto );
        } else if( dto.recorrencia( ).validade( ) != null ) {
            reagendarComRecorrencia( executionContext.getScheduler( ), dto );
        } else if ( dto.recorrencia( ).quantidade( ) > 0 ) {
            reagendarComRecorrenciaFinita( executionContext.getScheduler( ), dto );
        }

    }

    private void notificar( LocalDateTime proximaNotificacao, String jobId, String mensagem) {
        LocalDateTime agora = LocalDateTime.now( );
        if( proximaNotificacao.isEqual( agora ) || proximaNotificacao.isBefore( agora ) ) {
            LOGGER.info("Notificando job com id: {}", jobId);
            service.adicionarEnvio( mensagem );
//            producer.sendMessage(dto);
        }
    }

    //REAGENDA-------------------------------------------------------------------------------
    private void reagendarComDataEspecifica( Scheduler scheduler, LembreteOutputDTO dto )
    {
        List<LocalDateTime> notificacoesAgendadas = dto.datasEspecificas( );
        if ( notificacoesAgendadas.isEmpty( ) ) return;

        LocalDateTime proximaNotificacao = notificacoesAgendadas.get( 0 );
        Date proximaExecucao = Date.from( proximaNotificacao.atZone( ZoneId.systemDefault( ) ).toInstant( ) );
        Trigger novoTrigger = TriggerBuilder.newTrigger( )
                .withIdentity( dto.id( ).toString( ) )
                .startAt(proximaExecucao)
                .build( );
        reagendar(scheduler, dto, novoTrigger);
    }

    private void reagendarComRecorrencia( Scheduler scheduler, LembreteOutputDTO dto )
    {
        Trigger novoTrigger;
        RecorrenciaDTOOutput recorrencia = dto.recorrencia( );
        String cronExpression = dto.recorrencia( ).intervaloCronExp( );

        if( recorrencia.validade( ) != null )
            novoTrigger = montarTriggerComValidade( dto, cronExpression );

        novoTrigger = montarTriggerComCronExpression( dto, cronExpression );
        reagendar(scheduler, dto, novoTrigger);
    }

    private void reagendarComRecorrenciaFinita( Scheduler scheduler, LembreteOutputDTO dto )
    {
        RecorrenciaDTOOutput recorrencia = dto.recorrencia( );
        String cronExpression = dto.recorrencia( ).intervaloCronExp( );

        Trigger novoTrigger = montarTriggerComQuantidade(dto, cronExpression);
        reagendar(scheduler, dto, novoTrigger);
    }

    private static void reagendar(Scheduler scheduler, LembreteOutputDTO dto, Trigger novoTrigger) {
        try {
            scheduler.rescheduleJob( new TriggerKey( dto.id( ).toString( ) ), novoTrigger );
            LOGGER.info( "Reagendado job para {}", dto.id( ));
        }
        catch ( SchedulerException e ) {
            LOGGER.error("Erro ao reagendar job {}", dto.id( ), e);
        }
    }
    //------------------------------------------------------------------------------------------


    //MONTA AS TRIGGERS-------------------------------------------------------------------------
    private static Trigger montarTriggerComValidade(LembreteOutputDTO dto, String cronExpression) {
        Date validade = Date.from( dto.recorrencia( ).validade( ).atZone( ZoneId.systemDefault( ) ).toInstant( ) );
        return TriggerBuilder.newTrigger( )
                .withIdentity( dto.id( ).toString( ) )
                .withSchedule( CronScheduleBuilder.cronSchedule( cronExpression) )
                .endAt( validade )
                .build( );
    }

    private static Trigger montarTriggerComQuantidade(LembreteOutputDTO dto, String cronExpression) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, dto.recorrencia( ).quantidade( ) - 1); // Subtrai 1 porque o primeiro já conta
        Date validade = calendar.getTime();
        return TriggerBuilder.newTrigger( )
                .withIdentity( dto.id( ).toString( ) )
                .withSchedule( CronScheduleBuilder.cronSchedule( cronExpression) )
                .endAt( validade )
                .build( );
    }

    private static Trigger montarTriggerComCronExpression(LembreteOutputDTO dto, String cronExpression) {
        return TriggerBuilder.newTrigger( )
                .withIdentity( dto.id( ).toString( ) )
                .withSchedule( CronScheduleBuilder.cronSchedule(cronExpression) )
                .build( );
    }
}
