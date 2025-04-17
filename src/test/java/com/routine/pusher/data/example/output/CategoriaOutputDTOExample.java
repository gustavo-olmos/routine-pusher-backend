package com.routine.pusher.data.example.output;

import com.routine.pusher.data.model.dto.CategoriaOutputDTO;

public abstract class CategoriaOutputDTOExample
{
    private CategoriaOutputDTOExample( ) { }

    public static CategoriaOutputDTO simples( )
    {
        return  new CategoriaOutputDTO( 1L, "Importante", "Amarelo", 1 );
    }
}
