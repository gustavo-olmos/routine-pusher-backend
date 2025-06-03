package com.routine.pusher.infrastructure.common.helper;

import com.routine.pusher.core.enums.EnumDiasDaSemana;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CronExpessionBuilder
{
    public static String montarCronExprComDiasDaSemana( LocalDateTime momento, List<EnumDiasDaSemana> diasDaSemana )
    {
        String diasSemanaJoin = diasDaSemana.stream( )
                .map( EnumDiasDaSemana::getAbreviaturaIngles )
                .collect( Collectors.joining( "," ) );

        return String.format( "%d %d %d ? * %s",
                momento.getSecond( ), momento.getMinute( ), momento.getHour( ), diasSemanaJoin );
    }

    public static String montarCronExprComPosicaoDaSemana( LocalDateTime momento, int codigoDia, int posicaoDaSemanaNoMes )
    {
        return String.format( "%d %d %d ? * %d#%d",
                momento.getSecond( ), momento.getMinute( ), momento.getHour( ),
                codigoDia, posicaoDaSemanaNoMes );
    }

    public static String montarCronExprComDiaFixo( LocalDateTime momento, List<Integer> diasFixosNoMes )
    {
        String diasFixosJoin = diasFixosNoMes.stream( )
                .map( Object::toString )
                .collect( Collectors.joining( "," ) );

        return String.format( "%d %d %d %s * ?",
                momento.getSecond( ), momento.getMinute( ), momento.getHour( ), diasFixosJoin );
    }
}
