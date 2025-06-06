package com.routine.pusher.infrastructure.common.helper;

import com.routine.pusher.core.enums.EnumDiasDaSemana;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class CronExpessionBuilder
{
    public static String montarCronExprComDiasDaSemana( LocalTime horarioFixo, List<EnumDiasDaSemana> diasDaSemana )
    {
        String diasSemanaJoin = diasDaSemana.stream( )
                .map( EnumDiasDaSemana::getAbreviaturaIngles )
                .collect( Collectors.joining( "," ) );

        return String.format( "%d %d %d ? * %s",
                horarioFixo.getSecond( ), horarioFixo.getMinute( ), horarioFixo.getHour( ), diasSemanaJoin );
    }

    public static String montarCronExprComPosicaoDaSemana( LocalTime horarioFixo, int codigoDia, int posicaoDaSemanaNoMes )
    {
        return String.format( "%d %d %d ? * %d#%d",
                horarioFixo.getSecond( ), horarioFixo.getMinute( ), horarioFixo.getHour( ),
                codigoDia, posicaoDaSemanaNoMes );
    }

    public static String montarCronExprComDiaFixo(LocalTime momento, List<Integer> diasFixosNoMes )
    {
        String diasFixosJoin = diasFixosNoMes.stream( )
                .map( Object::toString )
                .collect( Collectors.joining( "," ) );

        return String.format( "%d %d %d %s * ?",
                momento.getSecond( ), momento.getMinute( ), momento.getHour( ), diasFixosJoin );
    }
}
