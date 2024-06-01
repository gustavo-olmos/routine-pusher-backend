package com.routine.pusher.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public class LembreteDTOBuilder
{
    private final LembreteDTO lembrete;

    private LembreteDTOBuilder( ) {
        lembrete = new LembreteDTO( );
    }

    public static LembreteDTOBuilder builder( ) {
        return new LembreteDTOBuilder( );
    }


    public LembreteDTO build( ) {
        return this.lembrete;
    }

    public LembreteDTOBuilder tarefa( TarefaDTO tarefa ) {
        this.lembrete.setTarefa( tarefa );
        return this;
    }

    public LembreteDTOBuilder categoria( CategoriaDTO categoria ) {
        this.lembrete.setCategoria( categoria );
        return this;
    }

    public LembreteDTOBuilder momentoNotificacao( List<LocalDateTime> momento ) {
        this.lembrete.setMomentoNotificacao( momento );
        return this;
    }

    public LembreteDTOBuilder intervaloRepeticao( LocalDateTime intervalo ) {
        this.lembrete.setIntervalo( intervalo );
        return this;
    }

    public LembreteDTOBuilder quantidade( int quantidade ) {
        this.lembrete.setQuantidade( quantidade );
        return this;
    }

    public LembreteDTOBuilder validade( LocalDateTime validade ) {
        this.lembrete.setValidade( validade );
        return this;
    }
}
