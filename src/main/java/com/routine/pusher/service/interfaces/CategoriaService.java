package com.routine.pusher.service.interfaces;

import com.routine.pusher.model.dto.CategoriaDTO;
import com.routine.pusher.util.SortInfo;

import java.util.List;

public interface CategoriaService
{
    List<CategoriaDTO> listar( String atributo, boolean ordemReversa );

    CategoriaDTO adicionar( CategoriaDTO dto );

    CategoriaDTO editar( CategoriaDTO dto );

    boolean excluir( Long id );
}
