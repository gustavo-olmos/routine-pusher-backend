package com.routine.pusher.service.interfaces;

import com.routine.pusher.model.dto.TarefaDTO;

import java.util.List;

public interface TarefaService
{
    List<TarefaDTO> listar( String atributo, boolean ordemReversa );

    TarefaDTO adicionar( TarefaDTO dto );

    TarefaDTO editar( Long id, TarefaDTO dto );

    boolean excluir( Long id );
}