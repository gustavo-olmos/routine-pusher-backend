package com.routine.pusher.model.enums;

public enum StatusConclusao
{
    CONCLUIDO(1),
    ABERTO(2);

    private final int value;

    StatusConclusao( int value ) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
