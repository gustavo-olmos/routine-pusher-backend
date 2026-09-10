package com.routine.pusher.core.domain.categoria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaInputDTO(
        @NotBlank(message = "O nome da categoria é obrigatório")
        // 25 é a largura da coluna (V6). Validar aqui transforma o que seria erro de dados no INSERT
        // em erro de campo, com o nome do campo na resposta.
        @Size(max = 25, message = "O nome da categoria deve ter no máximo 25 caracteres")
        String nome,

        @NotBlank(message = "A cor da categoria é obrigatória")
        String cor,

        int fatorOrdem
) { }
