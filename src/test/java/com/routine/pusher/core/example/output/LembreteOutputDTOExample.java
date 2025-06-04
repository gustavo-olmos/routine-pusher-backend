package com.routine.pusher.core.example.output;

import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaOutputDTO;
import com.routine.pusher.core.enums.EnumDiasDaSemana;

import java.time.LocalDateTime;
import java.util.List;

public abstract class LembreteOutputDTOExample
{
    private LembreteOutputDTOExample( ) { }

    public static LembreteOutputDTO simples( )
    {
        CategoriaOutputDTO categoria = new CategoriaOutputDTO( 1L, "Importante", "Amarelo", 1 );
        RecorrenciaOutputDTO recorrencia = new RecorrenciaOutputDTO(0, 0,
                9, 30, 0, null,
                List.of( EnumDiasDaSemana.SEGUNDA, EnumDiasDaSemana.QUARTA, EnumDiasDaSemana.SEXTA ),
                LocalDateTime.now( ).plusDays( 30L ) );

        return new LembreteOutputDTO( 1L, "TESTE", "Lembrete teste", "PENDENTE", categoria, recorrencia,
                null, List.of("vibração"), LocalDateTime.now( )  );
    }
}