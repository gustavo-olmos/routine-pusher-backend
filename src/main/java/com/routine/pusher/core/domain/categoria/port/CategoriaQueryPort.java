package com.routine.pusher.core.domain.categoria.port;

import com.routine.pusher.core.domain.categoria.Categoria;

import java.util.List;

public interface CategoriaQueryPort
{
    Categoria buscarPorId( Long id );

    /** Todas as categorias — o chat de IA as apresenta ao modelo para escolher o {@code categoriaId}. */
    List<Categoria> listar( );
}
