package com.routine.pusher.data.model.entities;

import com.routine.pusher.data.model.enums.EnumStatusConclusao;
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

    @Column(name = "dataCriacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "status")
    private String status = EnumStatusConclusao.PENDENTE.name( );

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaEntity categoria;

    @OneToOne
    @JoinColumn(name = "recorrencia_id")
    private RecorrenciaEntity recorrencia;

    @Column(name = "datasEspecificas")
    private List<LocalDateTime> datasEspecificas;

    @Column(name = "metodoNotificacao")
    private List<String> metodoNotificacao;
}
