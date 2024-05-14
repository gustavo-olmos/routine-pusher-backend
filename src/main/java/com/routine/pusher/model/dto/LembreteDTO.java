package com.routine.pusher.model.dto;

import com.routine.pusher.model.Tarefa;
import com.routine.pusher.model.Tempo;
import com.routine.pusher.model.Duracao;

import java.util.List;

public class LembreteDTO
{
    private static int id;

    private List<Tarefa> tarefas;
    private String categoria;
    private Tempo tempo;
    private Duracao duracao;

    public LembreteDTO( List<Tarefa> tarefas, String categoria, Duracao duracao ) {
        this.tarefas = tarefas;
        this.categoria = categoria;
        this.duracao = duracao;
    }

    public LembreteDTO( List<Tarefa> tarefas, String categoria, Tempo tempo, Duracao duracao ) {
        this.tarefas = tarefas;
        this.categoria = categoria;
        this.tempo = tempo;
        this.duracao = duracao;
    }

    public static int getId( ) { return id; }
    public static void setId( int id ) { LembreteDTO.id = id; }

    public List<Tarefa> getTarefas( ) { return tarefas; }
    public void setTarefas(List<Tarefa> tarefas) { this.tarefas = tarefas; }

    public String getCategoria( ) { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Tempo getTempo( ) { return tempo; }
    public void setTempo(Tempo tempo) { this.tempo = tempo; }

    public Duracao getDuracao( ) { return duracao; }
    public void setDuracao( Duracao duracao ) { this.duracao = duracao; }
}