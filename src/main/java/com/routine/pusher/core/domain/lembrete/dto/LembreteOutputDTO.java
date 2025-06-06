package com.routine.pusher.core.domain.lembrete.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaOutputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeListWrapper;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeWrapper;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record LembreteOutputDTO(
        Long id,

        String titulo,
        String descricao,
        String status,
        CategoriaOutputDTO categoria,
        RecorrenciaOutputDTO recorrencia,

        @JsonSerialize(using = LocalDateTimeListWrapper.LocalDateTimeListSerializer.class)
        @JsonDeserialize(using = LocalDateTimeListWrapper.LocalDateTimeListDeserializer.class)
        List<LocalDateTime> momentosEspecificados,

        @Schema(pattern = "HH:mm", example = "12:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horarioFixo,
        List<String> metodoNotificacao,

        @JsonSerialize(using = LocalDateTimeWrapper.LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeWrapper.LocalDateTimeDeserializer.class)
        LocalDateTime aPartirDe
) { }
