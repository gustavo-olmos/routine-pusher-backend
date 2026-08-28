package com.routine.pusher.infrastructure.exceptions;

/**
 * Runtime, como as demais exceções de regra tratadas pelo handler: obrigar {@code throws} em cada
 * camada só espalharia assinatura sem mudar o tratamento, que é centralizado no
 * {@code GlobalExceptionHandler} (422).
 */
public class ConversaoException extends RuntimeException
{
    public ConversaoException( String message )
    {
        super( "Falha ao converter json. Detalhes: " + message );
    }
}
