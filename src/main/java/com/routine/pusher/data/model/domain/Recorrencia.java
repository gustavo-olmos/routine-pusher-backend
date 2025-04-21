package com.routine.pusher.data.model.domain;

import com.routine.pusher.data.model.enums.EnumDiasDaSemana;
import com.routine.pusher.data.model.enums.EnumTipoRecorrencia;
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

    private int posicaoSemana;
    private EnumTipoRecorrencia tipoRecorrencia;
    private List<EnumDiasDaSemana> diasDaSemana;
    private EnumDiasDaSemana diaEspecificoSemana;
    private int diaFixoMes;
    private int intervaloMinutos;
    private int intervaloHoras;
    private int intervaloDias;

    public Recorrencia( )
    {
        this.intervaloCronExp = buildCronExpression( );
    }

    private String buildCronExpression( )
    {
        String segundos = "0";
        String minutos = intervaloMinutos > 0 ? "*/" + intervaloMinutos : "0";
        String horas = intervaloHoras > 0 ? "*/" + intervaloHoras : "12";
        String diasDoMes = "?";
        String mes = "*";
        String diasDaSemana = "*";

        assert tipoRecorrencia != null;
        if ( "MENSAL".equalsIgnoreCase( tipoRecorrencia.name( ) ) ) {
            if ( posicaoSemana > 0 && diaEspecificoSemana != null ) {
                diasDaSemana = diaEspecificoSemana.getValue( ) + "#" + posicaoSemana;
            } else if( diaFixoMes > 0 ) {
                diasDoMes = String.valueOf( diaFixoMes );
            }
        } else if( "SEMANAL".equalsIgnoreCase( tipoRecorrencia.name( ) ) ) {
            if ( diaEspecificoSemana != null )
                diasDaSemana = String.valueOf( diaEspecificoSemana.getValue( ) );
        } else if ( "DIARIO".equalsIgnoreCase( tipoRecorrencia.name( ) ) ) {
            diasDoMes = "*";
        } else if ( "QUINZENAL".equalsIgnoreCase( tipoRecorrencia.name( ) ) ) {
            diasDoMes = "1/15";
        }

        return String.format( "%s %s %s %s %s %s", segundos, minutos, horas, diasDoMes, mes, diasDaSemana );
    }
}
