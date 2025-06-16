package com.routine.pusher.core.domain.categoria;


import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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

    @OneToMany(mappedBy = "categoria")
    private List<LembreteEntity> lembrete;
}