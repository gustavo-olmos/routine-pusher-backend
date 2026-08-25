package com.routine.pusher.core.domain.lembrete.dto;

import com.routine.pusher.core.domain.notificacao.dto.NotificacaoInputDTO;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaInputDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LembreteInputDTO(
        @NotBlank(message = "O título do lembrete é obrigatório")
        String titulo,

        String descricao,

        @NotNull(message = "A categoria do lembrete é obrigatória")
        Long categoriaId,

        @Valid
        RecorrenciaInputDTO recorrencia,

        @NotNull(message = "A notificação do lembrete é obrigatória")
        @Valid
        NotificacaoInputDTO notificacao
) { }