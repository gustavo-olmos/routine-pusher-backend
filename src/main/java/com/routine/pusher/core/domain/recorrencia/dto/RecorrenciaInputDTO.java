package com.routine.pusher.core.domain.recorrencia.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import com.routine.pusher.core.enums.EnumPoliticaDiaUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

/**
 * As {@code @JsonPropertyDescription} entram no JSON Schema derivado para a integração de IA — ver
 * a nota em {@code LembreteInputDTO}.
 */
public record RecorrenciaInputDTO(
        @PositiveOrZero(message = "A quantidade não pode ser negativa")
        @JsonPropertyDescription("Quantos disparos no total; omitir para repetir sem limite")
        Integer quantidade,

        @PositiveOrZero(message = "O intervalo de dias não pode ser negativo")
        @JsonPropertyDescription("Repetir a cada N dias")
        Integer intervaloDias,

        @PositiveOrZero(message = "O intervalo de horas não pode ser negativo")
        @JsonPropertyDescription("Repetir a cada N horas")
        Integer intervaloHoras,

        @PositiveOrZero(message = "O intervalo de minutos não pode ser negativo")
        @JsonPropertyDescription("Repetir a cada N minutos")
        Integer intervaloMinutos,

        @JsonPropertyDescription("Semana do mês (1 a 5) quando a repetição é do tipo "
                + "'na segunda semana do mês'; usar junto com diasDaSemana")
        Integer posicaoDaSemanaNoMes,

        @JsonPropertyDescription("Dias fixos do mês (1 a 31) em que o lembrete dispara, "
                + "ex.: [5, 20] para todo dia 5 e dia 20")
        List<Integer> diasFixosNoMes,

        @JsonPropertyDescription("Dias da semana em que o lembrete dispara")
        List<EnumDiasDaSemana> diasDaSemana,

        @Schema(description = "O que fazer quando a ocorrência cai em fim de semana ou feriado "
                + "nacional. Exige recorrência de calendário ou intervalo de ao menos um dia",
                example = "PULAR")
        @JsonPropertyDescription("O que fazer quando a ocorrência cai em fim de semana ou feriado "
                + "nacional: PULAR descarta, ADIAR move para o próximo dia útil, ANTECIPAR move "
                + "para o dia útil anterior. Só vale com recorrência de calendário ou intervalo "
                + "de ao menos um dia; omitir se o usuário não pedir")
        EnumPoliticaDiaUtil politicaDiaUtil
){ }
