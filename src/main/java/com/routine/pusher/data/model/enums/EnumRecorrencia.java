package com.routine.pusher.data.model.enums;

import lombok.Getter;

@Getter
public enum EnumRecorrencia {

    DIARIO(1),
    SEGUNDA(2),
    TERCA(3),
    QUARTA(4),
    QUINTA(5),
    SEXTA(6),
    SABADO(7),
    DOMINGO(8),
    SEMANAL(9),
    QUINZENAL(10),
    MENSAL(11),
    ANUAL(12);

    private final int value;

    EnumRecorrencia( int value ) {
        this.value = value;
    }
}
