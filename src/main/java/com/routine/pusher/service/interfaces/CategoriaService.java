package com.routine.pusher.service.interfaces;

import com.routine.pusher.model.Categoria;

import java.util.List;

public interface CategoriaService
{
    List<Categoria> listar( );
    Categoria adicionar( String nome, String cor );
    Categoria editar( Categoria categoria, Long id );
    String excluir( Long id );
}
