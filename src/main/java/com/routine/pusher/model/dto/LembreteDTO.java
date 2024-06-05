package com.routine.pusher.model.dto;

import com.routine.pusher.model.enums.StatusConclusao;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class LembreteDTO
{
    private Long id;
    private String titulo;
    private String comentario;
    private List<SubtarefaDTO> subtarefa;
    private StatusConclusao status;
    private CategoriaDTO categoria;
    private List<LocalDateTime> momentoNotificacao;
    private LocalDateTime intervalo;
    private int quantidade;
    private LocalDateTime validade;


    public LembreteDTO( ) { }

    public static LembreteDTO builder( ) {
        return new LembreteDTO( );
    }
}