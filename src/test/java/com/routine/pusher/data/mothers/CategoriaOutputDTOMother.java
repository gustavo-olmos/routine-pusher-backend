package com.routine.pusher.data.mothers;

import com.routine.pusher.data.model.dto.CategoriaOutputDTO;

public abstract class CategoriaOutputDTOMother
{
    private CategoriaOutputDTOMother( ) { }

    public static CategoriaOutputDTO simples( )
    {
        return  new CategoriaOutputDTO( 1L, "Importante", "Amarelo", 1 );
    }
}
