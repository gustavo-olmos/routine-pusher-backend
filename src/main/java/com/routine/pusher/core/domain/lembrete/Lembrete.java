package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.application.job.AgendadorJob;
import com.routine.pusher.core.domain.categoria.Categoria;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.enums.EnumStatusConclusao;
import lombok.Data;

import java.time.LocalDateTime;

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
    private Notificacao notificacao;

    public Lembrete( )
    {
        this.dataCriacao = LocalDateTime.now( );
        this.status = EnumStatusConclusao.PENDENTE.name( );
    }


    public void setExecucao( )
    {
        if( notificacao.aindaTemNotificacao( this ) )
            notificacao.setProximaExecucao( notificacao.calcularProximaNotificacao( this ) );
    }

    public void agendarLembrete( )
    {
        AgendadorJob.agendar( this );
    }

    public void concluirLembrete( )
    {
        this.setStatus( EnumStatusConclusao.CONCLUIDO.name( ) );
    }
}