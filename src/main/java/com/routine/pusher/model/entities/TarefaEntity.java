package com.routine.pusher.model;

import com.routine.pusher.model.enums.StatusConclusao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Table(name = "tarefa")
@Entity(name = "Tarefa")
public class Tarefa
{
    @Id
    @Column(name = "tarefa_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private static Long id;

    @Column(name = "titulo", nullable = false )
    private String titulo;

    @Column(name = "comentario")
    private String comentario;

    @Column(name = "status")
    private StatusConclusao status;

    public Tarefa( String titulo, String comentario, StatusConclusao status ) {
        this.titulo = titulo;
        this.comentario = comentario;
        this.status = status;
    }
}
