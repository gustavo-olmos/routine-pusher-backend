package com.routine.pusher.data.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.routine.pusher.data.model.enums.EnumDiasDaSemana;
import com.routine.pusher.data.model.enums.EnumTipoRecorrencia;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeWrapper;

import java.time.LocalDateTime;
import java.util.List;

public record RecorrenciaInputDTO(
        int quantidade,
        int posicaoSemana,
        EnumTipoRecorrencia tipoRecorrencia,
        List<EnumDiasDaSemana> diasDaSemana,
        EnumDiasDaSemana diaEspecificoSemana,
        int diaFixoMes,
        int intervaloMinutos,
        int intervaloHoras,
        int intervaloDias,

        @JsonSerialize(using = LocalDateTimeWrapper.LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeWrapper.LocalDateTimeDeserializer.class)
        LocalDateTime validade
){ }