package com.routine.pusher.data.model.domain;


import lombok.Data;

import java.util.List;

@Data
public class Categoria
{
    private Long id;
    private String nome;
    private String cor;
    private int fatorOrdem;
    private List<Lembrete> lembrete;
}