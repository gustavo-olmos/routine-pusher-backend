package com.routine.pusher.model;

import java.util.List;

public class Lembrete
{
    private static int id;

    private Tempo tempo;
    private String categoria;
    private List<Tarefa> tarefas;

    public Lembrete( List<Tarefa> tarefas, String categoria )
    {
        this.tarefas = tarefas;
        this.categoria = categoria;
    }

    public Lembrete( List<Tarefa> tarefas, Tempo tempo, String categoria )
    {
        this.tarefas = tarefas;
        this.tempo = tempo;
        this.categoria = categoria;
    }

    public static int getId( ) { return id; }
    public static void setId( int id ) { Lembrete.id = id; }

    public Tempo getTempo( ) { return tempo; }
    public void setTempo(Tempo tempo) { this.tempo = tempo; }

    public String getCategoria( ) { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public List<Tarefa> getTarefas( ) { return tarefas; }
    public void setTarefas(List<Tarefa> tarefas) { this.tarefas = tarefas; }
}