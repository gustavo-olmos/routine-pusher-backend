package com.routine.pusher.exceptions;

public class ConversaoLembreteException extends Exception
{
    public ConversaoLembreteException( String message )
    {
        super("Falha ao converter json enviado para Lembrete. \n Detalhes: {}" + message);
    }
}
