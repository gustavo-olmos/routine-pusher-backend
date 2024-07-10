package com.routine.pusher.service.interfaces;

import com.routine.pusher.model.dto.CategoriaDTO;

import java.util.List;

public interface CategoriaService
{
    CategoriaDTO adicionar( CategoriaDTO dto );

    List<CategoriaDTO> listar( String atributo, boolean ordemReversa );

    CategoriaDTO editar( Long id, CategoriaDTO dto );

    boolean excluir( Long id );
}
