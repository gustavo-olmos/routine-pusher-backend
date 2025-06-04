package com.routine.pusher.core.domain.recorrencia.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeWrapper;

import java.time.LocalDateTime;
import java.util.List;

public record RecorrenciaInputDTO(
        Integer quantidade,
        Integer intervaloDias,
        Integer intervaloHoras,
        Integer intervaloMinutos,
        Integer posicaoDaSemanaNoMes,
        List<Integer> diasFixosNoMes,
        List<EnumDiasDaSemana> diasDaSemana,

        @JsonSerialize(using = LocalDateTimeWrapper.LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeWrapper.LocalDateTimeDeserializer.class)
        LocalDateTime validade
){ }

