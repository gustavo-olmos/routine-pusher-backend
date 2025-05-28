package com.routine.pusher.core.domain.recorrencia;

import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Table(name = "recorrencia")
public class RecorrenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "lembrete_id")
    private LembreteEntity lembrete;

    @Column(name = "validade")
    private LocalDateTime validade;

    @Column(name = "quantidade")
    private int quantidade;
}
