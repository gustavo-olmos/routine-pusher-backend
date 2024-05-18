package com.routine.pusher.model.dto;

import com.routine.pusher.model.enums.StatusConclusao;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TarefaDTO
{
    private Long id;
    private String titulo;
    private String descricao;
    private StatusConclusao status;
    private List<SubtarefaDTO> subtarefa;

    public TarefaDTO( String titulo, String descricao, StatusConclusao status ) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.status = status;
    }
}
