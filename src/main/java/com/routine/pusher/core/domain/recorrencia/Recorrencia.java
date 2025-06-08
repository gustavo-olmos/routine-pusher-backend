package com.routine.pusher.core.domain.recorrencia;

import com.routine.pusher.core.enums.EnumDiasDaSemana;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
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
}
