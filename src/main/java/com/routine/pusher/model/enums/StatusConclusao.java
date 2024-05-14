package com.routine.pusher.model.enums;

public enum StatusConclusao
{
    CONCLUIDO(1),
    INCOMPLETO(2),
    ATRASADO(3),
    CANCELADO(4),
    NAO_INICIADO(0);

    private int value;

    StatusConclusao( int value ) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
