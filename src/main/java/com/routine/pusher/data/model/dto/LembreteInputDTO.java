package com.routine.pusher.data.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LembreteInputDTO(
        String titulo,
        String descricao,
        String status,
        Long categoriaId,
        RecorrenciaDTO recorrencia,
        List<LocalDateTime> datasEspecificas,
        List<String> metodoNotificacao
) {}