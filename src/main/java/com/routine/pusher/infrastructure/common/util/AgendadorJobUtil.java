package com.routine.pusher.infrastructure.common.util;

import com.routine.pusher.application.job.ExecutorJob;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import org.quartz.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public final class AgendadorJobUtil
{
    private AgendadorJobUtil( ){ }

    public static JobDetail montarNovoJob( LembreteOutputDTO dto )
    {
        JobDataMap jobDataMap = new JobDataMap( );
        jobDataMap.put( dto.id( ).toString( ), dto );

        return JobBuilder.newJob( ExecutorJob.class )
                         .withIdentity( dto.id( ).toString( ) )
                         .setJobData( jobDataMap )
                         .build( );
    }

    public static Trigger montarNovoTrigger( LembreteOutputDTO dto )
    {
        if( dto.datasEspecificas( ).isEmpty( ) && dto.recorrencia( ) == null )
            throw new IllegalArgumentException("Não há notificações disponíveis para esse lembrete");

        LocalDateTime proximaNotificacao = dto.datasEspecificas( ).get( 0 );
        Date dataExecucao = Date.from( proximaNotificacao.atZone( ZoneId.systemDefault( ) ).toInstant( ) );

        return TriggerBuilder.newTrigger( )
                             .withIdentity( dto.id( ).toString( ) )
                             .startAt( dataExecucao )
                             .build( );
    }

    public static Trigger montarTriggerComValidade( LembreteOutputDTO dto, String cronExpression )
    {
        Date validade = Date.from( dto.recorrencia( ).validade( ).atZone( ZoneId.systemDefault( ) ).toInstant( ) );

        return TriggerBuilder.newTrigger( )
                .withIdentity( dto.id( ).toString( ) )
                .withSchedule( CronScheduleBuilder.cronSchedule( cronExpression) )
                .endAt( validade )
                .build( );
    }

    public static Trigger montarTriggerComQuantidade( LembreteOutputDTO dto, String cronExpression )
    {
        Calendar calendar = Calendar.getInstance( );
        calendar.add( Calendar.DATE, dto.recorrencia( ).quantidade( ) - 1 );
        Date validade = calendar.getTime( );

        return TriggerBuilder.newTrigger( )
                .withIdentity( dto.id( ).toString( ) )
                .withSchedule( CronScheduleBuilder.cronSchedule( cronExpression) )
                .endAt( validade )
                .build( );
    }

    public static Trigger montarTriggerComCronExpression( LembreteOutputDTO dto, String cronExpression )
    {
        return TriggerBuilder.newTrigger( )
                .withIdentity( dto.id( ).toString( ) )
                .withSchedule( CronScheduleBuilder.cronSchedule( cronExpression ) )
                .build( );
    }
}
