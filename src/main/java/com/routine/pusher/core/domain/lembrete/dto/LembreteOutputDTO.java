package com.routine.pusher.core.domain.lembrete.dto;

import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import com.routine.pusher.core.domain.notificacao.dto.NotificacaoOutputDTO;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaOutputDTO;

public record LembreteOutputDTO(
        Long id,

        String titulo,
        String descricao,
        String status,
        CategoriaOutputDTO categoria,
        RecorrenciaOutputDTO recorrencia,
        NotificacaoOutputDTO notificacao
) { }
