package com.routine.pusher.model.dto;

import com.routine.pusher.model.enums.StatusConclusao;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SubtarefaDTO
{
    private Long id;
    private String titulo;
    private StatusConclusao status;
}
