package com.routine.pusher.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoriaDTO
{
    private Long id;
    private String nome;
    private String cor;
    private int fatorOrdem;
}
