package com.routine.pusher.data.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeListWrapper;

import java.time.LocalDateTime;
import java.util.List;

public record LembreteOutputDTO(

        Long id,

        String titulo,

        String descricao,

        String status,

        CategoriaOutputDTO categoria,

        RecorrenciaDTOOutput recorrencia,

        @JsonSerialize(using = LocalDateTimeListWrapper.LocalDateTimeListSerializer.class)
        @JsonDeserialize(using = LocalDateTimeListWrapper.LocalDateTimeListDeserializer.class)
        List<LocalDateTime> datasEspecificas,

        List<String> metodoNotificacao
) { }
