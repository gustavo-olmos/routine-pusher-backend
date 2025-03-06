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

    @Column(name = "descricao")
    private String descricao;

    @OneToMany(mappedBy = "lembrete", fetch = FetchType.EAGER,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TarefaEntity> tarefas;

    @Column(name = "status")
    private StatusConclusao status;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaEntity categoria;

    @Column(name = "dataCriacao", nullable = true)
    private LocalDateTime dataCriacao;

    @Column(name = "momentoNotificacao")
    private List<LocalDateTime> momentoNotificacao;

    @Column(name = "intervalo")
    private LocalDateTime intervalo;

    @Column(name = "quantidade")
    private int quantidade;

    @Column(name = "validade")
    private LocalDateTime validade;

    public LembreteEntity() {
        this.dataCriacao = LocalDateTime.now();
    }
}
