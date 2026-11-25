package com.routine.pusher.core.domain.lembrete.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeListWrapper;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Rascunho que a IA montou, ainda não salvo. O {@code lembrete} é exatamente o corpo que o
 * {@code POST /api/v1/lembrete} aceita: confirmar é reenviá-lo, com ou sem ajuste do usuário.
 */
public record InterpretacaoLembreteOutputDTO(
        @Schema(description = "Lembrete montado a partir da frase, no formato de entrada da criação. "
                + "Para confirmar, envie este objeto ao POST /api/v1/lembrete")
        LembreteInputDTO lembrete,

        @JsonSerialize(using = LocalDateTimeListWrapper.LocalDateTimeListSerializer.class)
        @JsonDeserialize(using = LocalDateTimeListWrapper.LocalDateTimeListDeserializer.class)
        @Schema(description = "Prévia das próximas execuções, calculada pelo mesmo domínio que "
                + "a criação usa. Nada foi agendado")
        List<LocalDateTime> proximasExecucoes
) { }
