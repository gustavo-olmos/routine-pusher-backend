package com.routine.pusher.infrastructure.common.util;

import com.routine.pusher.application.job.ExecutorJob;
import com.routine.pusher.data.model.domain.Lembrete;
import org.quartz.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public final class AgendadorJobUtil
{
    private AgendadorJobUtil( ){ }

    public static JobDetail montarNovoJob( Lembrete lembrete )
    {
        JobDataMap jobDataMap = new JobDataMap( );
        jobDataMap.put( lembrete.getId( ).toString( ), lembrete );

        return JobBuilder.newJob( ExecutorJob.class )
                         .withIdentity( lembrete.getId( ).toString( ) )
                         .setJobData( jobDataMap )
                         .build( );
    }

    public static Trigger montarNovoTrigger( Lembrete lembrete )
    {
        if( lembrete.getDatasEspecificas( ).isEmpty( ) && lembrete.getRecorrencia( ) == null )
            throw new IllegalArgumentException("Não há notificações disponíveis para esse lembrete");

        LocalDateTime proximaNotificacao = lembrete.getDatasEspecificas( ).get( 0 );
        Date dataExecucao = Date.from( proximaNotificacao.atZone( ZoneId.systemDefault( ) ).toInstant( ) );

        return TriggerBuilder.newTrigger( )
                             .withIdentity( lembrete.getId( ).toString( ) )
                             .startAt( dataExecucao )
                             .build( );
    }

    public static Trigger montarTriggerComValidade( Lembrete lembrete, String cronExpression )
    {
        Date validade = Date.from( lembrete.getRecorrencia( ).getValidade( ).atZone( ZoneId.systemDefault( ) ).toInstant( ) );

        return TriggerBuilder.newTrigger( )
                .withIdentity( lembrete.getId( ).toString( ) )
                .withSchedule( CronScheduleBuilder.cronSchedule( cronExpression) )
                .endAt( validade )
                .build( );
    }

    public static Trigger montarTriggerComQuantidade( Lembrete lembrete, String cronExpression )
    {
        Calendar calendar = Calendar.getInstance( );
        calendar.add( Calendar.DATE, lembrete.getRecorrencia( ).getQuantidade( ) - 1 );
        Date validade = calendar.getTime( );

        return TriggerBuilder.newTrigger( )
                .withIdentity( lembrete.getId( ).toString( ) )
                .withSchedule( CronScheduleBuilder.cronSchedule( cronExpression) )
                .endAt( validade )
                .build( );
    }

    public static Trigger montarTriggerComCronExpression( Lembrete lembrete, String cronExpression )
    {
        return TriggerBuilder.newTrigger( )
                .withIdentity( lembrete.getId( ).toString( ) )
                .withSchedule( CronScheduleBuilder.cronSchedule( cronExpression ) )
                .build( );
    }
}
