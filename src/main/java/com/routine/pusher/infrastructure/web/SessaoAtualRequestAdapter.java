package com.routine.pusher.infrastructure.web;

import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
import com.routine.pusher.infrastructure.exceptions.ProcessoException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.UUID;

/**
 * Implementação da porta para o mundo servlet: lê o atributo que o {@link SessaoAnonimaFilter}
 * publicou na requisição corrente. Vive na infraestrutura porque "requisição corrente" é conceito
 * de servlet, não de domínio.
 */
@Component
public class SessaoAtualRequestAdapter implements SessaoAtualPort
{
    @Override
    public UUID uuid( )
    {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes( );

        Object uuid = atributos == null ? null
                : atributos.getAttribute( ATRIBUTO_REQUISICAO, RequestAttributes.SCOPE_REQUEST );

        if( uuid instanceof UUID u ) return u;

        throw new ProcessoException( "Sessão anônima",
                "Nenhuma sessão anônima no contexto — a rota passou pelo filtro de sessão?" );
    }
}
