package com.routine.pusher.core.domain.categoria.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaInputDTO(
        @NotBlank(message = "O nome da categoria é obrigatório")
        String nome,

        String cor,

        int fatorOrdem
) { }
