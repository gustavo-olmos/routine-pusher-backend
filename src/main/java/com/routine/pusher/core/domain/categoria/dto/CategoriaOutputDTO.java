package com.routine.pusher.core.domain.categoria.dto;

public record CategoriaOutputDTO(
        Long id,
        String nome,
        String cor,
        int fatorOrdem
) {}
