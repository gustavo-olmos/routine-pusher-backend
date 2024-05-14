package com.routine.pusher.model;

import java.time.LocalDateTime;
import java.util.List;

public class TempoBuilder
{
    private final Tempo tempo;


    private TempoBuilder( ) {
        tempo = new Tempo( );
    }

    public static TempoBuilder builder( ) {
        return new TempoBuilder( );
    }

    public Tempo build( ) {
        return this.tempo;
    }


    public TempoBuilder momentoNotificacao( LocalDateTime momentoNotificacao ) {
        this.tempo.setMomentoNotificacao( momentoNotificacao );
        return this;
    }

    public TempoBuilder frequencia( Frequencia frequencia ) {
        this.tempo.setFrequencia( frequencia );
        return this;
    }

    public TempoBuilder intervaloPersonalizado( List<LocalDateTime> intervaloPersonalizado ) {
        this.tempo.setIntervaloPersonalizado( intervaloPersonalizado );
        return this;
    }
}
