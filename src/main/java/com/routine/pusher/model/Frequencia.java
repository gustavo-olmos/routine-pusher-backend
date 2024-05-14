package com.routine.pusher.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;

@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
public class Frequencia
{
    private int minutos;
    private int horas;
    private int dias;
    private int semanas;
    private int meses;
    private int anos;
    private Calendar calendar;

    protected Frequencia( ) { }

    public static Frequencia builder ( ) {
        return new Frequencia( );
    }
}
