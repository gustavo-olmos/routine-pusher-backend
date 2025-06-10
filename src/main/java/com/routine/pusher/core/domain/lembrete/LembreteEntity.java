package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.core.domain.recorrencia.RecorrenciaEntity;
import com.routine.pusher.core.domain.categoria.CategoriaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter @Setter
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

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaEntity categoria;

    @Column(name = "metodo_notificacao")
    private List<String> metodoNotificacao;

    @OneToOne(mappedBy = "lembrete", fetch = FetchType.EAGER,
              cascade = CascadeType.ALL, orphanRemoval = true)
    private RecorrenciaEntity recorrencia;

    @Column(name = "horario")
    private LocalTime horario;

    @Column(name = "prox_notificacao")
    private LocalDateTime proxNotificacao;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Column(name = "datas_especificadas")
    private List<LocalDateTime> datasEpecificadas;
}
