package com.routine.pusher.core.example.input;

import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaInputDTO;
import com.routine.pusher.core.enums.EnumDiasDaSemana;

import java.util.List;

public abstract class LembreteInputDTOExample
{
    private LembreteInputDTOExample( ) { }

    public static LembreteInputDTO simples( )
    {
        RecorrenciaInputDTO recorrencia = new RecorrenciaInputDTO( null, 0, 0,
                List.of( EnumDiasDaSemana.SEGUNDA, EnumDiasDaSemana.QUARTA, EnumDiasDaSemana.SEXTA ),
                0, 0, 0, 0 );

        return new LembreteInputDTO( "TESTE", "Lembrete teste", "PENDENTE", 1L, recorrencia,
                null, List.of("vibração") );
    }
}
