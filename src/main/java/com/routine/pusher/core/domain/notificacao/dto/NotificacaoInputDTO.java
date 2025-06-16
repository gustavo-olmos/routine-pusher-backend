package com.routine.pusher.core.domain.notificacao.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeListWrapper;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeWrapper;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record NotificacaoInputDTO(
        List<String> metodo,

        @JsonFormat(pattern = "HH:mm")
        @Schema(pattern = "HH:mm", example = "00:00")
        LocalTime horario,

        @JsonSerialize(using = LocalDateTimeWrapper.LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeWrapper.LocalDateTimeDeserializer.class)
        LocalDateTime proximaExecucao,

        @JsonSerialize(using = LocalDateTimeWrapper.LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeWrapper.LocalDateTimeDeserializer.class)
        LocalDateTime ultimaExecucao,

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
