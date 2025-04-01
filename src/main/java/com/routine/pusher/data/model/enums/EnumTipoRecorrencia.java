package com.routine.pusher.data.model.enums;

import lombok.Getter;

@Getter
public enum EnumTipoRecorrencia {
    DIARIO(1),
    SEMANAL(2),
    QUINZENAL(3),
    MENSAL(4),
    CUSTOMIZADO(5);

    private final int value;

    EnumTipoRecorrencia( int value ) {
        this.value = value;
    }
}
