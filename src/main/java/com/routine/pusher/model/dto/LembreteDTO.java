package com.routine.pusher.model.dto;

import com.routine.pusher.model.Categoria;
import com.routine.pusher.model.Tarefa;
import com.routine.pusher.model.Tempo;
import com.routine.pusher.model.Duracao;

import java.util.List;

public record LembreteDTO(
        Long id,
        List<Tarefa> tarefas,
        Categoria categoria,
        Tempo tempo,
        Duracao duracao ){
}