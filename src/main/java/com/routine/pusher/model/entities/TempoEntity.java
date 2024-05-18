package com.routine.pusher.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
public class Tempo
{
    private LocalDateTime momentoNotificacao;
    private Frequencia frequencia;
    private List<LocalDateTime> intervaloPersonalizado;

    protected Tempo( ) { }
}
