package com.routine.pusher.data.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeWrapper;

import java.time.LocalDateTime;
import java.util.List;

public record RecorrenciaDTOInput(
        int quantidade,
        List<String> diasDaSemana,

        String intervaloMinutos,
        String intervaloHoras,
        String intervaloDias,

        @JsonSerialize(using = LocalDateTimeWrapper.LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeWrapper.LocalDateTimeDeserializer.class)
        LocalDateTime validade
){}
