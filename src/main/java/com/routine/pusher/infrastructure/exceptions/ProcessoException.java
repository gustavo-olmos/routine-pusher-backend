package com.routine.pusher.infrastructure.exceptions;

public class ProcessoException extends RuntimeException
{
    public ProcessoException( String message )
    {
        super( "Falha no processo de " + message + "." );
    }
}
