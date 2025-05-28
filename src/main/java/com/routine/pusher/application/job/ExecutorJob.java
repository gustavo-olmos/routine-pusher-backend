package com.routine.pusher.application.job;

import com.routine.pusher.application.service.interfaces.NotificadorSSEService;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.infrastructure.common.helper.AgendadorJobBuilder;
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

    private final NotificadorSSEService notificadorService;

    @Override
    public void execute( JobExecutionContext executionContext )
    {
        JobDataMap jobDataMap = executionContext.getJobDetail( ).getJobDataMap( );
        String jobId = executionContext.getJobDetail( ).getKey( ).getName( );

        Lembrete lembrete = obterLembrete( jobDataMap, jobId );
        if( lembrete == null ) return;

        List<LocalDateTime> notificacoesAgendadas = lembrete.getDatasEspecificas( );
        if( notificacoesAgendadas.isEmpty( ) )
            excluirJob( executionContext, jobId );

        notificar( notificacoesAgendadas.get( 0 ), jobId, lembrete.getTitulo( ) );
        reagendar(executionContext, lembrete);
    }


    @Nullable
    private static Lembrete obterLembrete( JobDataMap jobDataMap, String jobId )
    {
        Lembrete dto = (Lembrete) jobDataMap.get( jobId );
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
        }
    }

    private void reagendar( JobExecutionContext executionContext, Lembrete lembrete )
    {
        Recorrencia recorrencia = lembrete.getRecorrencia( );
        if ( recorrencia != null ) {
            if( recorrencia.getValidade( ) != null )
                reagendarComRecorrencia( executionContext.getScheduler( ), lembrete );

            if( recorrencia.getQuantidade( ) > 0 )
                reagendarComRecorrenciaFinita( executionContext.getScheduler( ), lembrete );
        } else {
            reagendarComDataEspecifica( executionContext.getScheduler( ), lembrete );
        }
    }

    private void reagendarComDataEspecifica( Scheduler scheduler, Lembrete lembrete )
    {
        List<LocalDateTime> notificacoesAgendadas = lembrete.getDatasEspecificas( );
        if ( notificacoesAgendadas.isEmpty( ) ) return;

        Trigger novoTrigger = AgendadorJobBuilder.montarNovoTrigger( lembrete );
        AgendadorJob.reagendar( scheduler, lembrete.getId( ).toString( ), novoTrigger );
    }

    private void reagendarComRecorrencia( Scheduler scheduler, Lembrete lembrete )
    {
        String cronExpression = lembrete.getRecorrencia( ).getIntervaloCronExp( );
        Trigger novoTrigger = ( lembrete.getRecorrencia( ).getValidade( ) != null )
            ? AgendadorJobBuilder.montarTriggerComValidade( lembrete, cronExpression )
            : AgendadorJobBuilder.montarTriggerComCronExpression( lembrete, cronExpression );

        AgendadorJob.reagendar( scheduler, lembrete.getId( ).toString( ), novoTrigger );
    }

    private void reagendarComRecorrenciaFinita( Scheduler scheduler, Lembrete lembrete )
    {
        String cronExpression = lembrete.getRecorrencia( ).getIntervaloCronExp( );
        Trigger novoTrigger = AgendadorJobBuilder.montarTriggerComQuantidade( lembrete, cronExpression );
        AgendadorJob.reagendar( scheduler, lembrete.getId( ).toString( ), novoTrigger );
    }
}