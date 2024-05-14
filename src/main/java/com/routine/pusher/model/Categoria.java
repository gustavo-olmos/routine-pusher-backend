package com.routine.pusher.model;

public class Categoria
{
    private static Long id;

    private String categoria;
    private String cor;

    public Categoria( String categoria, String cor )
    {
        this.categoria = categoria;
        this.cor = cor;
    }

    public String getCategoria( ) { return categoria; }
    public void setCategoria( String categoria ) { this.categoria = categoria; }

    public String getCor( ) { return cor; }
    public void setCor( String cor ) { this.cor = cor; }
}