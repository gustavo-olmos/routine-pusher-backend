package com.routine.pusher.service.interfaces;

import com.routine.pusher.model.dto.SubtarefaDTO;

import java.util.List;

public interface SubtarefaService
{
    List<SubtarefaDTO> listar( );

    List<SubtarefaDTO> adicionar( List<SubtarefaDTO> dto );

    SubtarefaDTO editar( Long id, SubtarefaDTO dto );

    boolean excluir( Long id );
}
