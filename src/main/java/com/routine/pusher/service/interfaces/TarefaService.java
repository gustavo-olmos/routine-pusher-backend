package com.routine.pusher.service.interfaces;

import com.routine.pusher.model.dto.TarefaDTO;

import java.util.List;
import java.util.Map;

public interface TarefaService
{
    TarefaDTO adicionar( TarefaDTO dto );

    TarefaDTO atualizar( Long id, TarefaDTO dto );

    boolean excluir( Long id );

    List<TarefaDTO> concluirTarefas( Map<Long, String> tarefasConcluidas );
}