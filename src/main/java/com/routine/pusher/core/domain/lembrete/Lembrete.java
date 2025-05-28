package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.application.job.AgendadorJob;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.domain.categoria.Categoria;
import com.routine.pusher.core.enums.EnumStatusConclusao;
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
    private String status;
    private Categoria categoria;
    private Recorrencia recorrencia;
    private List<LocalDateTime> datasEspecificas;
    private List<String> metodoNotificacao;

    public Lembrete( )
    {
        this.dataCriacao = LocalDateTime.now( );
        this.status = EnumStatusConclusao.PENDENTE.name( );
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
