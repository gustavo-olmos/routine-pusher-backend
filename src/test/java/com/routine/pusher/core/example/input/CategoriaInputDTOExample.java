package com.routine.pusher.core.example.input;

import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;

public abstract class CategoriaInputDTOExample
{
    private CategoriaInputDTOExample( ) { }

    public static CategoriaInputDTO simples( )
    {
        return new CategoriaInputDTO( "Importante", "Amarelo", 1 );
    }
}
