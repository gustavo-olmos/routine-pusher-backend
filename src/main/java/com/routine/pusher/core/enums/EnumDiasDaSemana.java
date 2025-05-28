package com.routine.pusher.core.enums;

import lombok.Getter;

@Getter
public enum EnumDiasDaSemana {

    SEGUNDA (1, "MON"),
    TERCA   (2, "TUE"),
    QUARTA  (3, "WED"),
    QUINTA  (4, "THU"),
    SEXTA   (5, "FRI"),
    SABADO  (6, "SAT"),
    DOMINGO (7, "SUN");

    private final int codigo;
    private final String abreviaturaIngles;

    EnumDiasDaSemana( int codigo, String abreviaturaIngles )
    {
        this.codigo = codigo;
        this.abreviaturaIngles = abreviaturaIngles;
    }
}
