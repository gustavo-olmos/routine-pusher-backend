package com.routine.pusher.core.domain.categoria.port;

import com.routine.pusher.core.domain.categoria.Categoria;

public interface CategoriaQueryPort
{
    Categoria buscarPorId( Long id );
}
