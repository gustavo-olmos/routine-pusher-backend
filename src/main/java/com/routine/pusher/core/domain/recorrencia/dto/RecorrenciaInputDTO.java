package com.routine.pusher.core.domain.recorrencia.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import com.routine.pusher.core.enums.EnumPoliticaDiaUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record RecorrenciaInputDTO(
        @PositiveOrZero(message = "A quantidade não pode ser negativa")
        Integer quantidade,

        @PositiveOrZero(message = "O intervalo de dias não pode ser negativo")
        Integer intervaloDias,

        @PositiveOrZero(message = "O intervalo de horas não pode ser negativo")
        Integer intervaloHoras,

        @PositiveOrZero(message = "O intervalo de minutos não pode ser negativo")
        Integer intervaloMinutos,

        Integer posicaoDaSemanaNoMes,
        List<Integer> diasFixosNoMes,
        List<EnumDiasDaSemana> diasDaSemana,

        @Schema(description = "O que fazer quando a ocorrência cai em fim de semana ou feriado "
                + "nacional. Exige recorrência de calendário ou intervalo de ao menos um dia",
                example = "PULAR")
        EnumPoliticaDiaUtil politicaDiaUtil
){ }

