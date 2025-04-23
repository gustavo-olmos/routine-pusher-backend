package com.routine.pusher.data.model.domain;

import com.routine.pusher.data.model.dto.RecorrenciaInputDTO;
import com.routine.pusher.data.model.enums.EnumDiasDaSemana;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Recorrencia
{
    private Long id;
    private String intervaloCronExp;
    private LocalDateTime validade;
    private int quantidade;

    private int numeroSemanaNoMes;
    private List<EnumDiasDaSemana> diasSemana;
    private int diaFixoNoMes;
    private int intervaloEmMinutos;
    private int intervaloEmHoras;
    private int intervaloEmDias;

    public Recorrencia( )
    {
        this.intervaloCronExp = buildCronExpression( );
    }

    //TODO: ajustar essa função para buildar as cron expressions corretamente
    public String buildCronExpression( )
    {
        String segundos = "0";
        String minutos = ( intervaloEmMinutos > 0 ) ? ("*/" + intervaloEmMinutos) : "0";
        String horas = ( intervaloEmHoras > 0 ) ? ("*/" + intervaloEmHoras) : "0";
        String diasMes = "?";
        String mes = "*";
        String diasSemana = "*";

        return String.format( "%s %s %s %s %s %s", segundos, minutos, horas, diasMes, mes, diasSemana );
    }

    public static Recorrencia fromInputDto( RecorrenciaInputDTO inputDTO )
    {
        Recorrencia recorrencia = new Recorrencia( );
        recorrencia.setValidade( inputDTO.validade( ) );
        recorrencia.setQuantidade( inputDTO.quantidade( ) );
        recorrencia.setNumeroSemanaNoMes( inputDTO.numeroSemanaNoMes( ) );
        recorrencia.setDiasSemana( inputDTO.diasSemana( ) );
        recorrencia.setDiaFixoNoMes( inputDTO.diaFixoNoMes( ) );
        recorrencia.setIntervaloEmMinutos( inputDTO.intervaloEmMinutos( ) );
        recorrencia.setIntervaloEmHoras( inputDTO.intervaloEmHoras( ) );
        recorrencia.setIntervaloEmDias( inputDTO.intervaloEmDias( ) );

        return recorrencia;
    }
}
