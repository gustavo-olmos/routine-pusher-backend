package com.routine.pusher.model;

import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;

@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
public class Frequencia
{
    @Column(name = "", nullable = false )
    private int minutos;
    @Column(name = "", nullable = false )
    private int horas;
    @Column(name = "", nullable = false )
    private int dias;
    @Column(name = "", nullable = false )
    private int semanas;
    @Column(name = "", nullable = false )
    private int meses;
    @Column(name = "", nullable = false )
    private int anos;
    private Calendar calendar;

    protected Frequencia( ) { }

    public static Frequencia builder ( ) {
        return new Frequencia( );
    }
}
