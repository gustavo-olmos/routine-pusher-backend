package com.routine.pusher.core.domain.sessao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "sessao_anonima")
public class SessaoAnonimaEntity
{
    // Chave interna: sequencial, estreita, nunca sai daqui — ver a mesma decisão em LembreteEntity.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Chave de negócio: o valor do cookie. updatable=false porque identidade que muda não é identidade.
    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "ultimo_acesso", nullable = false)
    private LocalDateTime ultimoAcesso;
}
