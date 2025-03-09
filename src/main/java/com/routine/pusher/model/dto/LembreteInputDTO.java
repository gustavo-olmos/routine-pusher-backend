package com.routine.pusher.model.dto;

import com.routine.pusher.model.enums.StatusConclusao;

import java.time.LocalDateTime;
import java.util.List;

public record LembreteInputDTO(
        String titulo,
        String descricao,
        List<TarefaDTO>tarefas,
        StatusConclusao status,
        CategoriaDTO categoria,
        List<LocalDateTime> momentoNotificacao,
        LocalDateTime intervalo,
        int quantidade,
        LocalDateTime validade
) { }
