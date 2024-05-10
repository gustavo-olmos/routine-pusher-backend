package com.routine.pusher.model;

import com.routine.pusher.model.enums.CondicaoRepeticao;
import com.routine.pusher.model.enums.Frequencia;

import java.time.LocalDateTime;

public class Tempo
{
    private LocalDateTime momentoNotificacao;
    private Frequencia frequencia;
    private CondicaoRepeticao condicaoRepeticao;

    public Tempo( LocalDateTime momentoNotificacao )
    {
        this.momentoNotificacao = momentoNotificacao;
    }

    public Tempo( LocalDateTime momentoNotificacao, Frequencia frequencia, CondicaoRepeticao condicaoRepeticao )
    {
        this.momentoNotificacao = momentoNotificacao;
        this.frequencia = frequencia;
        this.condicaoRepeticao = condicaoRepeticao;
    }

    public LocalDateTime getMomentoNotificacao( ) { return momentoNotificacao; }
    public void setMomentoNotificacao( LocalDateTime momentoNotificacao ) {this.momentoNotificacao = momentoNotificacao; }

    public Frequencia getFrequencia( ) { return frequencia; }
    public void setFrequencia( Frequencia frequencia ) { this.frequencia = frequencia; }

    public CondicaoRepeticao getCondicaoRepeticao() { return condicaoRepeticao; }
    public void setCondicaoRepeticao( CondicaoRepeticao condicaoRepeticao ) { this.condicaoRepeticao = condicaoRepeticao; }
}
