package com.routine.pusher.core.domain.recorrencia;

import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "intervalo_dias")
    private Integer intervaloDias;

    @Column(name = "intervalo_horas")
    private Integer intervaloHoras;

    @Column(name = "intervalo_minutos")
    private Integer intervaloMinutos;

    @Column(name = "pos_semana")
    private Integer posicaoDaSemanaNoMes;

    @Column(name = "dias_fixos")
    private List<Integer> diasFixosNoMes;

    @Column(name = "dias_semana")
    private List<EnumDiasDaSemana> diasDaSemana;
}
