package com.routine.pusher.data.example.input;

import com.routine.pusher.data.model.dto.CategoriaInputDTO;

public abstract class CategoriaInputDTOExample
{
    private CategoriaInputDTOExample( ) { }

    public static CategoriaInputDTO simples( )
    {
        return new CategoriaInputDTO( "Importante", "Amarelo", 1 );
    }
}
