package com.routine.pusher.infrastructure.common.util;

import com.routine.pusher.application.job.AgendadorJob;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import org.quartz.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public final class AgendadorJobUtil
{
    private AgendadorJobUtil( ) {}

    public static JobDetail montarNovoJob( LembreteOutputDTO dto )
    {
        JobDataMap jobDataMap = new JobDataMap( );
        jobDataMap.put( dto.id( ).toString( ), dto );

        return JobBuilder.newJob( AgendadorJob.class )
                         .withIdentity( dto.id( ).toString( ) )
                         .setJobData( jobDataMap )
                         .build( );
    }

    public static Trigger montarNovoTrigger( LembreteOutputDTO dto )
    {
        if( dto.datasEspecificas( ).isEmpty( ) && dto.recorrencia( ) == null ) {
            throw new IllegalArgumentException("Não há notificações disponíveis para esse lembrete");
        }

        LocalDateTime proximaNotificacao = dto.datasEspecificas( ).get( 0 );
        Date dataExecucao = Date.from( proximaNotificacao.atZone( ZoneId.systemDefault( ) ).toInstant( ) );

        return TriggerBuilder.newTrigger( )
                             .withIdentity( dto.id( ).toString( ) )
                             .startAt( dataExecucao )
                             .build( );
    }
}
