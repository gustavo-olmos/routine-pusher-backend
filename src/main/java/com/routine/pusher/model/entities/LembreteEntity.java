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

    @Column(name = "status")
    private StatusConclusao status;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaEntity categoria;

    @Column(name = "dataCriacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "momentoNotificacao")
    private List<LocalDateTime> momentoNotificacao;

    @Column(name = "recorrente")
    private boolean isRecorrente;

    @Column(name = "recorrencia")
    private List<String> recorrencia;


    public LembreteEntity() {
        this.dataCriacao = LocalDateTime.now();
    }
}
