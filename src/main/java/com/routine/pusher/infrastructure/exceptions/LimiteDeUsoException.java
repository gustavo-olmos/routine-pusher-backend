package com.routine.pusher.infrastructure.exceptions;

/**
 * Uso recusado por teto de recursos — hoje, o máximo de lembretes por sessão anônima. É exceção
 * própria, e não uma das de regra de negócio, porque o cliente reage diferente: não há nada para
 * corrigir no payload, o caminho é liberar espaço ou esperar.
 */
public class LimiteDeUsoException extends RuntimeException
{
    public LimiteDeUsoException( String mensagem )
    {
        super( mensagem );
    }
}
