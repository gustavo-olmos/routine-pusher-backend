package com.routine.pusher.core.domain.notificacao.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeListWrapper;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * As {@code @JsonPropertyDescription} entram no JSON Schema derivado para a integração de IA — ver
 * a nota em {@code LembreteInputDTO}.
 */
public record NotificacaoInputDTO(
        @NotEmpty(message = "Informe ao menos um método de notificação")
        @JsonPropertyDescription("Métodos de notificação, ex.: [\"pop-up\"]")
        List<String> metodo,

        @JsonFormat(pattern = "HH:mm")
        @Schema(pattern = "HH:mm", example = "00:00")
        @JsonPropertyDescription("Horário do disparo no formato HH:mm, quando a recorrência é por "
                + "calendário (dias da semana, dias fixos ou posição no mês)")
        LocalTime horario,

        @JsonSerialize(using = LocalDateTimeWrapper.LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeWrapper.LocalDateTimeDeserializer.class)
        @JsonPropertyDescription("Quando a série começa (e o momento do primeiro disparo em "
                + "recorrência por intervalo), formato ISO ex.: 2026-08-28T14:30:00")
        LocalDateTime dataInicio,

        @JsonSerialize(using = LocalDateTimeWrapper.LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeWrapper.LocalDateTimeDeserializer.class)
        @JsonPropertyDescription("Depois deste momento nada mais dispara; omitir se o usuário não "
                + "definir um fim")
        LocalDateTime dataFim,

        @JsonSerialize(using = LocalDateTimeListWrapper.LocalDateTimeListSerializer.class)
        @JsonDeserialize(using = LocalDateTimeListWrapper.LocalDateTimeListDeserializer.class)
        @JsonPropertyDescription("Datas exatas de disparo (formato ISO), quando o usuário aponta "
                + "os momentos a dedo em vez de uma regra; incompatível com recorrência")
        List<LocalDateTime> datasEspecificadas
) { }
