package com.routine.pusher.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Table(name = "categoria")
@Entity(name = "Categoria")
public class Categoria
{
    @Id
    @Column(name = "categoria_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private static Long id;

    @Column(name = "nome", nullable = false )
    private String nome;

    @Column(name = "cor", unique = true, nullable = false )
    private String cor;


    public Categoria( String nome, String cor ) {
        this.nome = nome;
        this.cor = cor;
    }
}