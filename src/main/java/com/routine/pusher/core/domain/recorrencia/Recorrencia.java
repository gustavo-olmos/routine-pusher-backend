package com.routine.pusher.core.domain.recorrencia;

import com.routine.pusher.core.domain.recorrencia.cron.CronExpressionBuilder;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recorrencia
{
    private Long id;

    private Integer quantidade;

    /*Sem CronExpression*/
    private Integer intervaloDias;
    private Integer intervaloHoras;
    private Integer intervaloMinutos;

    /*Com CronExpression*/
    private Integer posicaoDaSemanaNoMes;
    private List<Integer> diasFixosNoMes;
    private List<EnumDiasDaSemana> diasDaSemana;

    public Duration montarIntevalo( )
    {
        return Duration.ofDays( intervaloDias ).plusHours( intervaloHoras ).plusHours( intervaloHoras );
    }

    public String montarCronExpression( )
    {
        String cronExpression = "";

        if( !diasDaSemana.isEmpty( ) )
            cronExpression = CronExpressionBuilder.montarCronExprComDiasDaSemana( diasDaSemana );

        if( posicaoDaSemanaNoMes > 0 && diasDaSemana.size( ) == 1 ) {
            int codigoDia = diasDaSemana.get( 0 ).getCodigo( );
            cronExpression = CronExpressionBuilder.montarCronExprComPosicaoDaSemana( codigoDia , posicaoDaSemanaNoMes );
        }

        if( !diasFixosNoMes.isEmpty( ) )
            cronExpression = CronExpressionBuilder.montarCronExprComDiaFixo( diasFixosNoMes );

        return cronExpression;
    }

    public int proximoDiaDaSemana( int codigoDia )
    {
         return ( codigoDia == ( diasDaSemana.size( ) -1 ) )
                 ? diasDaSemana.get( 0 ).getCodigo( )
                 : diasDaSemana.get( codigoDia + 1 ).getCodigo( );
    }
}
