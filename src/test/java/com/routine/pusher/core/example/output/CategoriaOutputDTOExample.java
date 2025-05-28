package com.routine.pusher.core.example.output;

import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;

public abstract class CategoriaOutputDTOExample
{
    private CategoriaOutputDTOExample( ) { }

    public static CategoriaOutputDTO simples( )
    {
        return  new CategoriaOutputDTO( 1L, "Importante", "Amarelo", 1 );
    }
}
