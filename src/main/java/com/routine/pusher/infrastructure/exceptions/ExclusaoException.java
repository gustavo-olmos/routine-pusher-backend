package com.routine.pusher.infrastructure.exceptions;

public class ExclusaoException extends RuntimeException
{
    public ExclusaoException( String message )
    {
        super( "Erro ao remover item. \nDetalhes" + message );
    }
}
