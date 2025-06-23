package com.routine.pusher.core.domain.lembrete.dto;

import com.routine.pusher.core.domain.notificacao.dto.NotificacaoInputDTO;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaInputDTO;

public record LembreteInputDTO(
        String titulo,
        String descricao,
        Long categoriaId,
        RecorrenciaInputDTO recorrencia,
        NotificacaoInputDTO notificacao
) { }