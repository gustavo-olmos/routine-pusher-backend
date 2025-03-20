package com.routine.pusher.data.model.dto;

import com.routine.pusher.data.model.enums.StatusConclusao;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LembreteInputDTO
{
    private Long id;
    private String titulo;
    private String descricao;
    private StatusConclusao status;
    private CategoriaDTO categoria;
    private List<LocalDateTime> momentoNotificacao;
    private LocalDateTime intervalo;
    private int quantidade;
    private LocalDateTime validade;
}