package com.routine.pusher.data.model.domain;

import com.routine.pusher.application.job.AgendadorJob;
import com.routine.pusher.data.mapper.LembreteMapper;
import com.routine.pusher.data.model.enums.EnumStatusConclusao;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Lembrete
{
    private Long id;
    private LocalDateTime dataCriacao;
    private String titulo;
    private String descricao;
    private String status = EnumStatusConclusao.PENDENTE.name( );
    private Categoria categoria;
    private Recorrencia recorrencia;
    private List<LocalDateTime> datasEspecificas;
    private List<String> metodoNotificacao;
    private LembreteMapper mapper;

    public Lembrete( )
    {
        this.dataCriacao = LocalDateTime.now( );
    }

    public void concluirLembrete( )
    {
        this.setStatus( EnumStatusConclusao.CONCLUIDO.name( ) );
    }

    public void agendarLembrete( )
    {
        AgendadorJob.agendar( this );
    }
}
