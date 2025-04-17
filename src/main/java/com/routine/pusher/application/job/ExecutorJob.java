package com.routine.pusher.application.job;

import com.routine.pusher.application.service.interfaces.NotificadorSSEService;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.data.model.dto.RecorrenciaOutputDTO;
import com.routine.pusher.infrastructure.common.util.AgendadorJobUtil;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@AllArgsConstructor
public class ExecutorJob implements Job
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorJob.class);

    //private final RabbitMQProducer producer;
    private final NotificadorSSEService notificadorService;

    @Override
    public void execute( JobExecutionContext executionContext )
    {
        JobDataMap jobDataMap = executionContext.getJobDetail( ).getJobDataMap( );
        String jobId = executionContext.getJobDetail( ).getKey( ).getName( );

        LembreteOutputDTO dto = obterLembrete( jobDataMap, jobId );
        if( dto == null ) return;

        List<LocalDateTime> notificacoesAgendadas = dto.datasEspecificas( );
        if( notificacoesAgendadas.isEmpty( ) )
            excluirJob( executionContext, jobId );

        notificar( notificacoesAgendadas.get( 0 ), jobId, dto.titulo( ) );
        reagendar(executionContext, dto);
    }


    @Nullable
    private static LembreteOutputDTO obterLembrete( JobDataMap jobDataMap, String jobId )
    {
        LembreteOutputDTO dto = (LembreteOutputDTO) jobDataMap.get( jobId );
        if( dto == null ) {
            LOGGER.error("LembreteDTO não encontrado para o job ID: {}", jobId);
            return null;
        }
        return dto;
    }

    private static void excluirJob( JobExecutionContext executionContext, String jobId )
    {
        try {
            LOGGER.info("Nenhuma notificação pendente para o job {}", jobId);
            executionContext.getScheduler( ).deleteJob( executionContext.getJobDetail( ).getKey( ) );
        } catch ( SchedulerException ex ) {
            LOGGER.error("Erro ao remover job {}", ex.getMessage( ));
            throw new RuntimeException( );
        }
    }

    private void notificar( LocalDateTime proximaNotificacao, String jobId, String mensagem)
    {
        LocalDateTime agora = LocalDateTime.now( );
        if( proximaNotificacao.isEqual( agora ) || proximaNotificacao.isBefore( agora ) ) {
            LOGGER.info("Notificando job com id: {}", jobId);
            notificadorService.adicionarEnvio( mensagem );
            //producer.sendMessage(dto);
        }
    }

    private void reagendar( JobExecutionContext executionContext, LembreteOutputDTO dto )
    {
        RecorrenciaOutputDTO recorrencia = dto.recorrencia( );
        if ( recorrencia != null ) {
            if( recorrencia.validade( ) != null )
                reagendarComRecorrencia( executionContext.getScheduler( ), dto );

            if( recorrencia.quantidade( ) > 0 )
                reagendarComRecorrenciaFinita( executionContext.getScheduler( ), dto );
        } else {
            reagendarComDataEspecifica( executionContext.getScheduler( ), dto );
        }
    }

    private void reagendarComDataEspecifica( Scheduler scheduler, LembreteOutputDTO dto )
    {
        List<LocalDateTime> notificacoesAgendadas = dto.datasEspecificas( );
        if ( notificacoesAgendadas.isEmpty( ) ) return;

        Trigger novoTrigger = AgendadorJobUtil.montarNovoTrigger( dto );
        AgendadorJob.reagendar( scheduler, dto.id( ).toString( ), novoTrigger );
    }

    private void reagendarComRecorrencia( Scheduler scheduler, LembreteOutputDTO dto )
    {
        String cronExpression = dto.recorrencia( ).intervaloCronExp( );
        Trigger novoTrigger = ( dto.recorrencia( ).validade( ) != null )
            ? AgendadorJobUtil.montarTriggerComValidade( dto, cronExpression )
            : AgendadorJobUtil.montarTriggerComCronExpression( dto, cronExpression );
        AgendadorJob.reagendar( scheduler, dto.id( ).toString( ), novoTrigger );
    }

    private void reagendarComRecorrenciaFinita( Scheduler scheduler, LembreteOutputDTO dto )
    {
        String cronExpression = dto.recorrencia( ).intervaloCronExp( );
        Trigger novoTrigger = AgendadorJobUtil.montarTriggerComQuantidade( dto, cronExpression );
        AgendadorJob.reagendar( scheduler, dto.id( ).toString( ), novoTrigger );
    }
}