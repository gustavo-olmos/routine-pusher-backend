package com.routine.pusher.model.dto;

import com.routine.pusher.model.enums.StatusConclusao;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TarefaDTO
{
    private Long id;
    private Long lembreteId;
    private String titulo;
    private StatusConclusao status;
}
