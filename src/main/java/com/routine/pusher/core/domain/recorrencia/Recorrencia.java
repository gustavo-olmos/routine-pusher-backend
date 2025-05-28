package com.routine.pusher.core.domain.recorrencia;

import com.routine.pusher.core.enums.EnumDiasDaSemana;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recorrencia
{
    private Long id;

    private int quantidade;
    private LocalDateTime validade;
    private LocalDateTime aPartirDe;
    private LocalDateTime momentoDinamico;

    private int intervaloHoras;
    private int intervaloMinutos;

    private List<EnumDiasDaSemana> diasDaSemana;
    private int posicaoDaSemanaNoMes;
    private List<Integer> diasFixosNoMes;
    private String cronExpression;


    public String buildCronExprDiasDaSemana( LocalDateTime momento )
    {
        String diasSemanaJoin = diasDaSemana.stream( )
                .map( EnumDiasDaSemana::getAbreviaturaIngles )
                .collect( Collectors.joining( "," ) );

        cronExpression = String.format( "%d %d %d ? * %s",
            momento.getSecond( ), momento.getMinute( ), momento.getHour( ), diasSemanaJoin );

        return cronExpression;
    }

    public String buildCronExprComPosicaoDaSemana( LocalDateTime momento, int posicao )
    {
        if( diasDaSemana.size() == 1) {
            cronExpression = String.format("%d %d %d ? * %d#%d",
                momento.getSecond( ), momento.getMinute( ), momento.getHour( ),
                diasDaSemana.get( 0 ).getCodigo( ), posicao );
        }

        return cronExpression;
    }

    public String buildCronExprComDiaFixo( LocalDateTime momento )
    {
        String diasFixosJoin = diasFixosNoMes.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));

        cronExpression = String.format( "%d %d %d %s * ?",
            momento.getSecond(), momento.getMinute(), momento.getHour(), diasFixosJoin);

        return  cronExpression;
    }
}
