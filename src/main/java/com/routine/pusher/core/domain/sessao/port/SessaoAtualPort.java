package com.routine.pusher.core.domain.sessao.port;

import java.util.UUID;

/**
 * De onde os serviços descobrem a sessão dona da requisição em andamento. É porta, e não acesso
 * direto ao request, porque quem consome (serviço, mapper) não deve saber que a sessão veio de um
 * cookie — se um dia ela vier de um token, nada aqui muda.
 */
public interface SessaoAtualPort
{
    /** Nome do atributo de request onde o filtro publica o UUID da sessão resolvida. */
    String ATRIBUTO_REQUISICAO = "sessaoAnonimaUuid";

    /**
     * UUID da sessão da requisição atual. Falha alto fora de uma requisição HTTP: código de job não
     * tem sessão, e depender dela lá é erro de programação, não condição de runtime.
     */
    UUID uuid( );
}
