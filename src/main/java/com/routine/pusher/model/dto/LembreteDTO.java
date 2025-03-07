package com.routine.pusher.model.dto;

import com.routine.pusher.model.enums.StatusConclusao;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class LembreteDTO
{
    private Long id;
    private String titulo;
    private String descricao;
    private List<TarefaDTO> tarefas;
    private StatusConclusao status;
    private CategoriaDTO categoria;
    private List<LocalDateTime> momentoNotificacao;
    private LocalDateTime intervalo;
    private int quantidade;
    private LocalDateTime validade;
}