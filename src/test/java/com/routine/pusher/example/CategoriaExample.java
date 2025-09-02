package com.routine.pusher.example;

import com.routine.pusher.core.domain.categoria.CategoriaEntity;
import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;

public abstract class CategoriaExample {

    private CategoriaExample() {}

    public static CategoriaInputDTO inputDTO( )
    {
        return new CategoriaInputDTO( "Importante", "Amarelo", 1 );
    }

    public static CategoriaOutputDTO outputDTO( )
    {
        return new CategoriaOutputDTO( 1L, "Importante", "Amarelo", 1 );
    }

    public static CategoriaEntity entity( )
    {
        CategoriaEntity entity = new CategoriaEntity( );
        entity.setId( 1L );
        entity.setNome( "Importante" );
        entity.setCor( "Amarelo" );
        entity.setFatorOrdem( 1 );

        return entity;
    }
}
