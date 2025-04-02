package com.routine.pusher.data.mothers;

import com.routine.pusher.data.model.dto.CategoriaInputDTO;

public abstract class CategoriaInputDTOMother
{
    private CategoriaInputDTOMother( ) { }

    public static CategoriaInputDTO simples( )
    {
        return  new CategoriaInputDTO( 1L, "Importante", "Amarelo", 1 );
    }
}
