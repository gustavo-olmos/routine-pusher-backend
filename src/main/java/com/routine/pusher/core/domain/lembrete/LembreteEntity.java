package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.core.domain.notificacao.NotificacaoEntity;
import com.routine.pusher.core.domain.recorrencia.RecorrenciaEntity;
import com.routine.pusher.core.domain.categoria.CategoriaEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Entity
@Table(name = "lembrete")
public class LembreteEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataCriacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now( );

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "status")
    private String status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntity categoria;

    @OneToOne(mappedBy = "lembrete", fetch = FetchType.EAGER,
              cascade = CascadeType.ALL, orphanRemoval = true)
    private RecorrenciaEntity recorrencia;

    @OneToOne(mappedBy = "lembrete", fetch = FetchType.EAGER,
              cascade = CascadeType.ALL, orphanRemoval = true)
    private NotificacaoEntity notificacao;
}
