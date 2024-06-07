package com.routine.pusher.model.entities;

import com.routine.pusher.model.enums.StatusConclusao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "subtarefa")
public class TarefaEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn( name = "lembrete_id" )
    private LembreteEntity lembrete;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "status")
    private StatusConclusao status;
}
