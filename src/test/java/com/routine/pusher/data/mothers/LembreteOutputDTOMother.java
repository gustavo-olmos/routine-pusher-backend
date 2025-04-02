package com.routine.pusher.data.mothers;

import com.routine.pusher.data.model.dto.CategoriaOutputDTO;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.data.model.dto.RecorrenciaOutputDTO;

import java.util.List;

public abstract class LembreteOutputDTOMother
{
    private LembreteOutputDTOMother( ) { }

    public static LembreteOutputDTO simples( )
    {
        CategoriaOutputDTO categoria = new CategoriaOutputDTO( 1L, "Importante", "Amarelo", 1 );
        RecorrenciaOutputDTO recorrencia = new RecorrenciaOutputDTO( 0, "0 0 1/12 * ? *", null );

        return new LembreteOutputDTO( 1L, "TESTE", "Lembrete teste", "PENDENTE", categoria, recorrencia,
                null, List.of("vibração")  );
    }
}