package com.routine.pusher.infrastructure.exceptions;

public class ConversaoException extends Exception
{
    public ConversaoException(String message )
    {
        super("Falha ao converter json. \n Detalhes: {}" + message);
    }
}
