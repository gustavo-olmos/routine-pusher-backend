package com.routine.pusher.application.service.interfaces;

import com.routine.pusher.data.model.dto.CategoriaOutputDTO;

import java.util.List;

public interface CategoriaService
{
    CategoriaOutputDTO adicionar(CategoriaOutputDTO dto );

    List<CategoriaOutputDTO> listar(String atributo, boolean ordemReversa );

    CategoriaOutputDTO atualizar(Long id, CategoriaOutputDTO dto );

    boolean excluir( Long id );
}
