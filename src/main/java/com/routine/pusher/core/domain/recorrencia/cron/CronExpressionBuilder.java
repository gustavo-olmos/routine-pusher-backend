package com.routine.pusher.core.domain.recorrencia.cron;

import com.routine.pusher.core.enums.EnumDiasDaSemana;

import java.util.List;
import java.util.stream.Collectors;

public class CronExpressionBuilder
{
    public static String montarCronExprComDiasDaSemana( List<EnumDiasDaSemana> diasDaSemana )
    {
        String diasSemanaJoin = diasDaSemana.stream( )
                .map( EnumDiasDaSemana::getAbreviaturaIngles )
                .collect( Collectors.joining( "," ) );

        return String.format( " ? * %s", diasSemanaJoin );
    }

    public static String montarCronExprComPosicaoDaSemana( int codigoDia, int posicaoDaSemanaNoMes )
    {
        return String.format( " ? * %d#%d", codigoDia, posicaoDaSemanaNoMes );
    }

    public static String montarCronExprComDiaFixo( List<Integer> diasFixosNoMes )
    {
        String diasFixosJoin = diasFixosNoMes.stream( )
                .map( Object::toString )
                .collect( Collectors.joining( "," ) );

        return String.format( " %s * ?", diasFixosJoin );
    }
}
