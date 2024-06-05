package com.routine.pusher.model.entities;

import com.routine.pusher.model.enums.StatusConclusao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@Entity
@Table(name = "lembrete")
public class LembreteEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "comentario")
    private String comentario;

    @OneToOne(mappedBy = "lembrete")
    private List<SubtarefaEntity> subTarefa;

    @Column(name = "status")
    private StatusConclusao status;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaEntity categoria;

    @Column(name = "dataCriacao", unique = true, nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "momentoNotificacao")
    private List<LocalDateTime> momentoNotificacao;

    @Column(name = "intervalo")
    private LocalDateTime intervalo;

    @Column(name = "quantidade")
    private int quantidade;

    @Column(name = "validade")
    private LocalDateTime validade;
}
