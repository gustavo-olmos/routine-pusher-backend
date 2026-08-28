package com.routine.pusher.core.domain.sessao.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * O que o front precisa saber da própria sessão: quem ela é e até quando vale. {@code expiraEm} é
 * calculado do último acesso — cada chamada à API renova a validade, e esta resposta reflete isso.
 */
public record SessaoOutputDTO( UUID uuid, LocalDateTime criadaEm, LocalDateTime expiraEm )
{
}
