package com.routine.pusher.model;

import com.routine.pusher.model.enums.StatusConclusao;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tarefa
{
    private static Long id;

    private String titulo;
    private String comentario;
    private StatusConclusao status;

    public Tarefa( String titulo, String comentario, StatusConclusao status ) {
        this.titulo = titulo;
        this.comentario = comentario;
        this.status = status;
    }
}
