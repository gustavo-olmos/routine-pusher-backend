package com.routine.pusher.core.domain.categoria;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "categoria")
public class CategoriaEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "cor", unique = true, nullable = false)
    private String cor;

    @Column(name = "fatorOrdem", unique = true, nullable = false)
    private int fatorOrdem;
}