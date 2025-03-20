package com.routine.pusher.data.model.dto;

import com.routine.pusher.data.model.enums.StatusConclusao;

import java.time.LocalDateTime;
import java.util.List;

public record LembreteOutputDTO(
        Long id,
        String titulo,
        String descricao,
        List<TarefaDTO> tarefas,
        StatusConclusao status,
        CategoriaDTO categoria,
        List<LocalDateTime> momentoNotificacao
) { }
