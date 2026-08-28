package com.routine.pusher.core.domain.lembrete.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Entrada do chat de lembrete: uma frase em linguagem natural e, opcionalmente, o relógio de quem
 * pede.
 */
public record LembreteChatInputDTO(
        @NotBlank(message = "A frase do lembrete é obrigatória")
        @Size(max = 500, message = "A frase deve ter no máximo 500 caracteres")
        @Schema(description = "O lembrete descrito em linguagem natural",
                example = "me lembra de beber água a cada 2 horas até as 18h")
        String frase,

        @JsonSerialize(using = LocalDateTimeWrapper.LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeWrapper.LocalDateTimeDeserializer.class)
        @Schema(description = "Data e hora locais de quem pede, para resolver expressões como "
                + "'amanhã às 9h' no fuso do usuário — o servidor pode estar em outro. Se omitido, "
                + "vale o relógio do servidor", example = "2026-08-28T14:30:00")
        LocalDateTime agora
) { }
