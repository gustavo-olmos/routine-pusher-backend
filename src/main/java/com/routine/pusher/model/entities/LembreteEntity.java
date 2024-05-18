package com.routine.pusher.model.entities;

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

    @OneToOne(mappedBy = "lembrete")
    private TarefaEntity tarefa;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaEntity categoria;

    @Column(name = "dataCriacao", unique = true, nullable = false)
    private String dataCriacao;

    @Column(name = "momentoNotificacao")
    private List<LocalDateTime> momentoNotificacao;

    @Column(name = "intervalo")
    private LocalDateTime intervalo;

    @Column(name = "quantidade")
    private int quantidade;

    @Column(name = "validade")
    private LocalDateTime validade;
}
