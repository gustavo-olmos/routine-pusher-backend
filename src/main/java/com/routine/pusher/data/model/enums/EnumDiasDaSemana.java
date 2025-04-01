package com.routine.pusher.data.model.enums;

import lombok.Getter;

@Getter
public enum EnumDiasDaSemana {

    SEGUNDA(1),
    TERCA(2),
    QUARTA(3),
    QUINTA(4),
    SEXTA(5),
    SABADO(6),
    DOMINGO(7);

    private final int value;

    EnumDiasDaSemana( int value ) {
        this.value = value;
    }
}
