package com.routine.pusher.model;

import java.util.Calendar;

public class FrequenciaBuilder
{
    private final Frequencia frequencia;


    private FrequenciaBuilder( ) {
        frequencia = new Frequencia( );
    }

    public static FrequenciaBuilder builder( ) {
        return new FrequenciaBuilder( );
    }

    public Frequencia build( ) {
        return this.frequencia;
    }


    public FrequenciaBuilder minutos( int minutos ) {
        this.frequencia.setMinutos( minutos );
        return this;
    }

    public FrequenciaBuilder horas( int horas ) {
        this.frequencia.setHoras( horas );
        return this;
    }

    public FrequenciaBuilder dias( int dias ) {
        this.frequencia.setDias( dias );
        return this;
    }

    public FrequenciaBuilder semanas( int semanas ) {
        this.frequencia.setSemanas( semanas );
        return this;
    }

    public FrequenciaBuilder meses( int meses ) {
        this.frequencia.setMeses( meses );
        return this;
    }

    public FrequenciaBuilder anos( int anos ) {
        this.frequencia.setAnos( anos );
        return this;
    }

    public FrequenciaBuilder calendar( Calendar calendar ) {
        this.frequencia.setCalendar( calendar );
        return this;
    }
}
