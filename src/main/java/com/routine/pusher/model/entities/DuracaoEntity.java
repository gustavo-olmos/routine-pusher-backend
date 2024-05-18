package com.routine.pusher.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Duracao
{
    private int quantidade;
    private LocalDateTime validade;


    public Duracao( int quantidade ) {
        this.quantidade = quantidade;
    }

    public Duracao( LocalDateTime validade ) {
        this.validade = validade;
    }
}
