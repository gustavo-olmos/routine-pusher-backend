package com.routine.pusher.application.service.interfaces;

import com.routine.pusher.data.model.dto.CategoriaInputDTO;
import com.routine.pusher.data.model.dto.CategoriaOutputDTO;

import java.util.List;

public interface CategoriaService
{
    CategoriaOutputDTO adicionar( CategoriaInputDTO dto );

    List<CategoriaOutputDTO> listar( String atributo, boolean ordemReversa );

    CategoriaOutputDTO atualizar( Long id, CategoriaInputDTO dto );

    boolean excluir( Long id );
}
