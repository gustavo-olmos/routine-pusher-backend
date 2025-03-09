package com.routine.pusher.util;

import com.routine.pusher.jobs.AgendadorJob;
import com.routine.pusher.model.dto.LembreteDTO;
import org.quartz.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public final class AgendadorJobUtil
{
    private AgendadorJobUtil( ) {}

    public static JobDetail montarNovoJob(LembreteDTO dto )
    {
        JobDataMap jobDataMap = new JobDataMap( );
        jobDataMap.put( dto.getId( ).toString( ), dto );

        return JobBuilder.newJob( AgendadorJob.class )
                         .withIdentity( dto.getId( ).toString( ) )
                         .setJobData( jobDataMap )
                         .build( );
    }

    public static Trigger montarNovoTrigger(LembreteDTO dto )
    {
        if( dto.getMomentoNotificacao( ).isEmpty( ) ) {
            throw new IllegalArgumentException("Não há notificações disponíveis para esse lembrete");
        }

        LocalDateTime proximaNotificacao = dto.getMomentoNotificacao( ).get( 0 );
        Date dataExecucao = Date.from( proximaNotificacao.atZone( ZoneId.systemDefault( ) ).toInstant( ) );

        return TriggerBuilder.newTrigger( )
                             .withIdentity( dto.getId( ).toString( ) )
                             .startAt( dataExecucao )
                             .build( );
    }
}
