package com.routine.pusher.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class LembreteDTO
{
    private Long id;
    private TarefaDTO tarefa;
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