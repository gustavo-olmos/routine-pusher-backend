package com.routine.pusher.core.domain.recorrencia;

import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "recorrencia")
public class RecorrenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "lembrete_id")
    private LembreteEntity lembrete;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recorrencia_dias_fixos", joinColumns = @JoinColumn(name = "recorrencia_id"))
    @OrderColumn(name = "ordem")
    @Column(name = "dia_fixo")
    private List<Integer> diasFixosNoMes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recorrencia_dias_semana", joinColumns = @JoinColumn(name = "recorrencia_id"))
    @OrderColumn(name = "ordem")
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana")
    private List<EnumDiasDaSemana> diasDaSemana;
}
