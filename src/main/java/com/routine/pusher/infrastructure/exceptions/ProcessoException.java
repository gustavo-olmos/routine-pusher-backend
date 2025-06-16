package com.routine.pusher.infrastructure.exceptions;

public class ProcessoException extends RuntimeException
{
    public ProcessoException( String message, String detalhes )
    {
        super( "Falha no processo de " + message + ". \nDetalhes: " + detalhes );
    }
}
