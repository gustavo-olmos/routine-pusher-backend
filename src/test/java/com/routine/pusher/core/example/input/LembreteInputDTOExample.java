package com.routine.pusher.core.example.input;

import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaInputDTO;
import com.routine.pusher.core.enums.EnumDiasDaSemana;

import java.time.LocalDateTime;
import java.util.List;

public abstract class LembreteInputDTOExample
{
    private LembreteInputDTOExample( ) { }

    public static LembreteInputDTO simples( )
    {
        RecorrenciaInputDTO recorrencia = new RecorrenciaInputDTO(0,
                0, 9, 30, 0, null,
                List.of( EnumDiasDaSemana.SEGUNDA, EnumDiasDaSemana.QUARTA, EnumDiasDaSemana.SEXTA ),
                LocalDateTime.now( ).plusDays( 30L ) );

        return new LembreteInputDTO( "TESTE", "Lembrete teste", "PENDENTE", 1L, recorrencia,
                List.of("vibração"), LocalDateTime.now( ), null );
    }
}
