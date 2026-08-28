package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.core.domain.notificacao.NotificacaoEntity;
import com.routine.pusher.core.domain.recorrencia.RecorrenciaEntity;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaEntity;
import com.routine.pusher.core.domain.categoria.CategoriaEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "lembrete")
public class LembreteEntity
{
    // Chave interna: sequencial, estreita, nunca sai daqui.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Chave de negócio: é por ela que a aplicação procura o lembrete. updatable=false porque
    // identidade pública que muda não é identidade.
    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(name = "dataCriacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now( );

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "status")
    private String status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntity categoria;

    // Dona do lembrete. EAGER como as demais associações desta entidade — e por necessidade: o
    // executor de jobs converte a entidade em domínio fora de uma requisição HTTP, onde um proxy
    // LAZY já não teria sessão do Hibernate para se resolver.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sessao_id", nullable = false)
    private SessaoAnonimaEntity sessao;

    @OneToOne(mappedBy = "lembrete", fetch = FetchType.EAGER,
              cascade = CascadeType.ALL, orphanRemoval = true)
    private RecorrenciaEntity recorrencia;

    @OneToOne(mappedBy = "lembrete", fetch = FetchType.EAGER,
              cascade = CascadeType.ALL, orphanRemoval = true)
    private NotificacaoEntity notificacao;
}
