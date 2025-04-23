package com.routine.pusher.data.model.entities;

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

    @Column(name = "intervalo_cron_exp")
    private String intervaloCronExp;

    @Column(name = "validade")
    private LocalDateTime validade;

    @Column(name = "quantidade")
    private int quantidade;
}
