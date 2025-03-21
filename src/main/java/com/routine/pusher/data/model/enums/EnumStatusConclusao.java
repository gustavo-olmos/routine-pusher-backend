package com.routine.pusher.data.model.enums;

import lombok.Getter;

@Getter
public enum EnumStatusConclusao
{
    PENDENTE( 1 ),
    CONCLUIDO( 2 );

    private final int value;

    EnumStatusConclusao( int value ) {
        this.value = value;
    }
}
