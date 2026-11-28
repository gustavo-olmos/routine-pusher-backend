package com.routine.pusher.infrastructure.exceptions;

import java.time.Duration;

/**
 * O provedor de IA não atendeu — ou a API decidiu nem chamá-lo. Não é culpa de quem digitou: não há
 * nada a corrigir na frase, e o formulário continua funcionando sem IA. Por isso vira 503 com
 * {@code Retry-After}, e não 422.
 *
 * <p>{@code recusaDoProvedor} separa o 429 do provedor (cota ou taxa estourada: vale pausar as
 * próximas chamadas) de uma falha de rede pontual (não vale).</p>
 */
public class IaIndisponivelException extends RuntimeException
{
    private final Duration tenteEm;
    private final boolean recusaDoProvedor;

    public IaIndisponivelException( String mensagem, Duration tenteEm, boolean recusaDoProvedor )
    {
        super( mensagem );
        this.tenteEm = tenteEm;
        this.recusaDoProvedor = recusaDoProvedor;
    }

    public Duration tenteEm( )
    {
        return tenteEm;
    }

    public boolean recusaDoProvedor( )
    {
        return recusaDoProvedor;
    }
}
