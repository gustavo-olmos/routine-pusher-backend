package com.routine.pusher.core.domain.lembrete.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaInputDTO;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeListWrapper;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeWrapper;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record LembreteInputDTO(
        String titulo,
        String descricao,
        Long categoriaId,
        List<String> metodoNotificacao,
        RecorrenciaInputDTO recorrencia,

        @JsonFormat(pattern = "HH:mm")
        @Schema(pattern = "HH:mm", example = "00:00")
        LocalTime horario,

        @JsonSerialize(using = LocalDateTimeWrapper.LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeWrapper.LocalDateTimeDeserializer.class)
        LocalDateTime dataInicio,

        @JsonSerialize(using = LocalDateTimeWrapper.LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeWrapper.LocalDateTimeDeserializer.class)
        LocalDateTime dataFim,

        @JsonSerialize(using = LocalDateTimeListWrapper.LocalDateTimeListSerializer.class)
        @JsonDeserialize(using = LocalDateTimeListWrapper.LocalDateTimeListDeserializer.class)
        List<LocalDateTime> datasEspecificadas
) { }