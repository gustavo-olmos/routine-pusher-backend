package com.routine.pusher.core.domain.notificacao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacao
{
    private Long id;
    private List<String> metodo;
    private LocalTime horario;
    private LocalDateTime proximaExecucao;
    private LocalDateTime ultimaExecucao;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private List<LocalDateTime> datasEspecificadas;
}
