package com.routine.pusher.model.dto;

import com.routine.pusher.model.enums.StatusConclusao;

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



    public LembreteDTOBuilder titulo( String titulo )
    {
        this.lembrete.setTitulo( titulo );
        return this;
    }

    public LembreteDTOBuilder comentario( String comentario )
    {
        this.lembrete.setComentario( comentario );
        return this;
    }

    public LembreteDTOBuilder subtarefa( SubtarefaDTO subtarefa )
    {
        this.lembrete.setSubtarefa( subtarefa );
        return this;
    }

    public LembreteDTOBuilder status( StatusConclusao status )
    {
        this.lembrete.setStatus( status );
        return this;
    }

    public LembreteDTOBuilder categoria( CategoriaDTO categoria )
    {
        this.lembrete.setCategoria( categoria );
        return this;
    }

    public LembreteDTOBuilder momentoNotificacao( List<LocalDateTime> momento )
    {
        this.lembrete.setMomentoNotificacao( momento );
        return this;
    }

    public LembreteDTOBuilder intervaloRepeticao( LocalDateTime intervalo )
    {
        this.lembrete.setIntervalo( intervalo );
        return this;
    }

    public LembreteDTOBuilder quantidade( int quantidade )
    {
        this.lembrete.setQuantidade( quantidade );
        return this;
    }

    public LembreteDTOBuilder validade( LocalDateTime validade )
    {
        this.lembrete.setValidade( validade );
        return this;
    }
}
