package com.routine.pusher.core.domain.categoria;


import com.routine.pusher.core.domain.lembrete.Lembrete;
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