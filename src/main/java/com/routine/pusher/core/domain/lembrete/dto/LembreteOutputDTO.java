package com.routine.pusher.core.domain.lembrete.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import com.routine.pusher.core.domain.notificacao.dto.NotificacaoOutputDTO;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaOutputDTO;
import com.routine.pusher.infrastructure.common.shared.LocalDateTimeListWrapper;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record LembreteOutputDTO(
        @Schema(description = "Identidade pública do lembrete, e a única pela qual ele é consultado. "
                + "O id sequencial da tabela não é exposto", example = "3f1a…")
        UUID uuid,

        String titulo,
        String descricao,
        String status,
        CategoriaOutputDTO categoria,
        RecorrenciaOutputDTO recorrencia,
        NotificacaoOutputDTO notificacao,

        @JsonSerialize(using = LocalDateTimeListWrapper.LocalDateTimeListSerializer.class)
        @JsonDeserialize(using = LocalDateTimeListWrapper.LocalDateTimeListDeserializer.class)
        @Schema(description = "Prévia das próximas execuções derivada da recorrência. Não é estado "
                + "persistido: é recalculada a cada leitura e respeita validade e cota restante")
        List<LocalDateTime> proximasExecucoes
) { }
