package com.routine.pusher.application.service;

import com.routine.pusher.data.model.dto.CategoriaDTO;

import java.util.List;

public interface CategoriaService
{
    CategoriaDTO adicionar( CategoriaDTO dto );

    List<CategoriaDTO> listar( String atributo, boolean ordemReversa );

    CategoriaDTO atualizar( Long id, CategoriaDTO dto );

    boolean excluir( Long id );
}
