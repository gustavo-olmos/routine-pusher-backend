package com.routine.pusher.data.mapper;

import com.routine.pusher.data.model.dto.LembreteInputDTO;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.data.model.dto.RecorrenciaInputDTO;
import com.routine.pusher.data.model.entities.LembreteEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", builder = @Builder( disableBuilder = true ))
public interface LembreteMapper
{
    LembreteMapper INSTANCE = Mappers.getMapper( LembreteMapper.class );

    LembreteOutputDTO toOutputDto( LembreteEntity lembrete );

    @Mapping(target = "categoria.id", source = "categoriaId")
    @Mapping(target = "recorrencia.intervaloCronExp", expression = "java( buildCronExpression( recorrenciaInputDTO ) )")
    LembreteEntity toEntity( LembreteInputDTO lembrete );

    default String buildCronExpression( RecorrenciaInputDTO recorrencia )
    {
        if( recorrencia == null ) return null;

        String segundos = "0";
        String minutos = recorrencia.intervaloMinutos( ) > 0 ? "*/" + recorrencia.intervaloMinutos( ) : "0";
        String horas = recorrencia.intervaloHoras( ) > 0 ? "*/" + recorrencia.intervaloHoras( ) : "12";
        String diasDoMes = "?";
        String mes = "*";
        String diasDaSemana = "*";

        if ( "MENSAL".equalsIgnoreCase( recorrencia.tipoRecorrencia( ).name( ) ) ) {
            if ( recorrencia.posicaoSemana( ) > 0 && recorrencia.diaEspecificoSemana( ) != null ) {
                diasDaSemana = recorrencia.diaEspecificoSemana( ).getValue( ) + "#" + recorrencia.posicaoSemana();
            } else if( recorrencia.diaFixoMes( ) > 0 ) {
                diasDoMes = String.valueOf( recorrencia.diaFixoMes( ) );
            }
        } else if( "SEMANAL".equalsIgnoreCase( recorrencia.tipoRecorrencia( ).name( ) ) ) {
            if ( recorrencia.diaEspecificoSemana( ) != null ) {
                diasDaSemana = String.valueOf( recorrencia.diaEspecificoSemana( ).getValue( ) );
            }
        } else if ( "DIARIO".equalsIgnoreCase( recorrencia.tipoRecorrencia( ).name( ) ) ) {
            diasDoMes = "*";
        } else if ( "QUINZENAL".equalsIgnoreCase(recorrencia.tipoRecorrencia( ).name( ) ) ) {
            diasDoMes = "1/15";
        }

        return String.format( "%s %s %s %s %s %s", segundos, minutos, horas, diasDoMes, mes, diasDaSemana );
    }
}
