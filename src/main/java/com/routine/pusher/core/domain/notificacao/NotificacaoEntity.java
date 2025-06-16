package com.routine.pusher.core.domain.notificacao;

import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Entity
@Table(name = "notificacao")
public class NotificacaoEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "lembrete_id")
    private LembreteEntity lembrete;

    @Column(name = "metodo")
    private List<String> metodo;

    @Column(name = "horario")
    private LocalTime horario;

    @Column(name = "proxima_execucao")
    private LocalDateTime proximaExecucao;

    @Column(name = "ultima_execucao")
    private LocalDateTime ultimaExecucao;

    @Column(name = "dt_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "dt_fim")
    private LocalDateTime dataFim;

    @Column(name = "dts_especificadas")
    private List<LocalDateTime> datasEspecificadas;
}
