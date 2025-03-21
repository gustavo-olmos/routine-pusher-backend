package com.routine.pusher.data.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LembreteOutputDTO(
        Long id,
        String titulo,
        String descricao,
        String status,
        CategoriaOutputDTO categoria,
        RecorrenciaDTO recorrencia,
        List<LocalDateTime> datasEspecificas,
        List<String> metodoNotificacao
) { }
