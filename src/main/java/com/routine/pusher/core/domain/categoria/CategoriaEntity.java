package com.routine.pusher.core.domain.categoria;


import com.routine.pusher.core.domain.sessao.SessaoAnonimaEntity;
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

    @Column(name = "nome", nullable = false, length = 25)
    private String nome;

    // A unicidade de cor e ordem é por lista, não global — declarada na tabela (V6) e não no campo,
    // porque é composta com a sessão: dois visitantes podem ter a mesma cor.
    @Column(name = "cor", nullable = false)
    private String cor;

    @Column(name = "fatorOrdem", nullable = false)
    private int fatorOrdem;

    /**
     * Dona da categoria. EAGER como as demais associações do projeto, e sem cascata: quem desmonta
     * uma sessão é o {@code SessaoService}, na ordem que os agendamentos exigem.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sessao_id", nullable = false)
    private SessaoAnonimaEntity sessao;
}