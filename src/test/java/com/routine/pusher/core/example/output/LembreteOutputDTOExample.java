package com.routine.pusher.core.example.output;

import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaOutputDTO;

import java.util.List;

public abstract class LembreteOutputDTOExample
{
    private LembreteOutputDTOExample( ) { }

    public static LembreteOutputDTO simples( )
    {
        CategoriaOutputDTO categoria = new CategoriaOutputDTO( 1L, "Importante", "Amarelo", 1 );
        RecorrenciaOutputDTO recorrencia = new RecorrenciaOutputDTO( 0, "0 0 1/12 * ? *", null );

        return new LembreteOutputDTO( 1L, "TESTE", "Lembrete teste", "PENDENTE", categoria, recorrencia,
                null, List.of("vibração")  );
    }
}