package com.routine.pusher.application.external.client;

import com.routine.pusher.core.domain.categoria.Categoria;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Porta para o provedor de IA que transforma linguagem natural em lembrete. O serviço não sabe qual
 * modelo atende do outro lado — trocar de provedor é trocar a implementação, o resto (contexto,
 * validação, criação) não se move.
 *
 * <p>O contexto vem por parâmetro, e não é descoberto aqui, porque ele é regra da aplicação: o
 * "agora" pertence a quem pediu (fuso do usuário) e as categorias vêm da porta de consulta.</p>
 */
public interface ChatClient<T>
{
    /**
     * @param frase      o lembrete descrito pelo usuário em linguagem natural
     * @param agora      o instante local de quem pede — referência para "amanhã às 9h"
     * @param categorias categorias existentes, para o modelo escolher o {@code categoriaId}
     */
    T montarLembrete( String frase, LocalDateTime agora, List<Categoria> categorias );
}
