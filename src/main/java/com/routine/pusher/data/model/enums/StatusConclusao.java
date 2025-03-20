package com.routine.pusher.data.model.enums;

import lombok.Getter;

@Getter
public enum StatusConclusao
{
    CONCLUIDO(1),
    ABERTO(2);

    private final int value;

    StatusConclusao( int value ) {
        this.value = value;
    }
}
