package com.routine.pusher.data.example.input;

import com.routine.pusher.data.model.dto.LembreteInputDTO;
import com.routine.pusher.data.model.dto.RecorrenciaInputDTO;
import com.routine.pusher.data.model.enums.EnumDiasDaSemana;

import java.util.List;

public abstract class LembreteInputDTOExample
{
    private LembreteInputDTOExample( ) { }

    public static LembreteInputDTO simples( )
    {
        RecorrenciaInputDTO recorrencia = new RecorrenciaInputDTO( 0, 0, null,
                List.of(EnumDiasDaSemana.SEGUNDA, EnumDiasDaSemana.QUARTA, EnumDiasDaSemana.SEXTA), null,
                0, 0, 0, 0, null );

        return new LembreteInputDTO( "TESTE", "Lembrete teste", "PENDENTE", 1L, recorrencia,
                null, List.of("vibração") );
    }
}
