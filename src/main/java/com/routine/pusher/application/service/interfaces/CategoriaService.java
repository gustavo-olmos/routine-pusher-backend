package com.routine.pusher.application.service.interfaces;

import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;

import java.util.List;

public interface CategoriaService
{
    CategoriaOutputDTO adicionar( CategoriaInputDTO inputDto );

    List<CategoriaOutputDTO> listar( String atributo, boolean ordemReversa );

    CategoriaOutputDTO atualizar( Long id, CategoriaInputDTO inputDto);

    boolean excluir( Long id );
}
