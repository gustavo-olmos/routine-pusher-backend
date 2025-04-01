package com.routine.pusher.data.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeListWrapper;

import java.time.LocalDateTime;
import java.util.List;

public record LembreteInputDTO(
        String titulo,

        String descricao,

        String status,

        Long categoriaId,

        RecorrenciaDTOInput recorrencia,

        @JsonSerialize(using = LocalDateTimeListWrapper.LocalDateTimeListSerializer.class)
        @JsonDeserialize(using = LocalDateTimeListWrapper.LocalDateTimeListDeserializer.class)
        List<LocalDateTime> datasEspecificas,

        List<String> metodoNotificacao
) {}