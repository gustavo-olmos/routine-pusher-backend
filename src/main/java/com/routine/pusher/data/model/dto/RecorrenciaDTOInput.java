package com.routine.pusher.data.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.routine.pusher.data.model.enums.EnumDiasDaSemana;
import com.routine.pusher.data.model.enums.EnumTipoRecorrencia;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeWrapper;

import java.time.LocalDateTime;
import java.util.List;

public record RecorrenciaDTOInput(
        int quantidade,                       // Quantidade de eventos
        int posicaoSemana,                    // Posição da semana (ex: 2a, 3a semana)
        EnumTipoRecorrencia tipoRecorrencia,  // Tipo de recorrência (DIARIO, SEMANAL, QUINZENAL, MENSAL)
        List<EnumDiasDaSemana> diasDaSemana,  // Dias da semana em que o evento ocorre
        EnumDiasDaSemana diaEspecificoSemana,           // Dia específico da semana (ex: SEGUNDA, QUINTA)
        int diaFixoMes,                       // Dia fixo do mês (ex: 15)
        int intervaloMinutos,                 // Intervalo de tempo em minutos
        int intervaloHoras,                   // Intervalo de tempo em horas
        int intervaloDias,                    // Intervalo de tempo em dias

        @JsonSerialize(using = LocalDateTimeWrapper.LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeWrapper.LocalDateTimeDeserializer.class)
        LocalDateTime validade
){}
