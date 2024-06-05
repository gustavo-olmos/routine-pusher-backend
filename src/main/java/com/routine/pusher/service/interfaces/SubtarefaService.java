package com.routine.pusher.service.interfaces;

import com.routine.pusher.model.dto.SubtarefaDTO;

import java.util.List;

public interface SubtarefaService
{
    List<SubtarefaDTO> listar( );

    SubtarefaDTO adicionar( SubtarefaDTO dto );

    SubtarefaDTO editar( Long id, SubtarefaDTO dto );

    boolean excluir( Long id );
}
