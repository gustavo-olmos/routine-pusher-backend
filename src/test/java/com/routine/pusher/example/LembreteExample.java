package com.routine.pusher.example;

import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;

public abstract class LembreteExample {

    private LembreteExample() {}

    public static LembreteInputDTO simplesInput( )
    {
//        RecorrenciaInputDTO recorrencia = new RecorrenciaInputDTO( 0, 0,
//                9, 30, 0, null,
//                List.of( EnumDiasDaSemana.SEGUNDA, EnumDiasDaSemana.QUARTA, EnumDiasDaSemana.SEXTA ),
//                LocalDateTime.now( ).plusDays( 30L ) );
        return null;
//
//        return new LembreteInputDTO( "TESTE", "Lembrete teste",
//                "PENDENTE", 1L, recorrencia, List.of( LocalDateTime.now( ) ),
//                LocalTime.of(15, 10),  List.of("vibração"), LocalDateTime.now( ) );
    }

    public static LembreteOutputDTO simplesOutput( )
    {
//        CategoriaOutputDTO categoria = new CategoriaOutputDTO( 1L, "Importante", "Amarelo", 1 );
//        RecorrenciaOutputDTO recorrencia = new RecorrenciaOutputDTO(0, 0,
//                9, 30, 0, null,
//                List.of( EnumDiasDaSemana.SEGUNDA, EnumDiasDaSemana.QUARTA, EnumDiasDaSemana.SEXTA ),
//                LocalDateTime.now( ).plusDays( 30L ) );
//
//        return new LembreteOutputDTO( 1L, "TESTE", "Lembrete teste",
//                "PENDENTE", categoria, recorrencia, null,
//                LocalTime.of(15, 10), List.of("vibração"), LocalDateTime.now( )  );
        return null;
    }
}
