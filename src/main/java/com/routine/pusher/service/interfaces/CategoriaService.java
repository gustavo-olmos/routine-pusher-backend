package com.routine.pusher.service.interfaces;

import com.routine.pusher.model.dto.CategoriaDTO;

import java.util.List;

public interface CategoriaService
{
    List<CategoriaDTO> listar( );

    CategoriaDTO adicionar( String nome, String cor );

    CategoriaDTO editar( CategoriaDTO categoria, Long id );

    String excluir( Long id );
}
