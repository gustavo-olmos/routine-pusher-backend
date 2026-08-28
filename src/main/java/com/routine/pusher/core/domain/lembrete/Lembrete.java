package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.core.domain.categoria.Categoria;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.enums.EnumStatusConclusao;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Lembrete
{
    /**
     * Quantas execuções futuras a saída da API antecipa por lembrete.
     */
    public static final int LIMITE_PROXIMAS_EXECUCOES = 5;

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

    public List<LocalDateTime> calcularProximasExecucoes( int limite )
    {
        if( notificacao == null ) return List.of( );

        return notificacao.calcularProximasNotificacoes( this, limite );
    }

    public void concluirLembrete( )
    {
        this.setStatus( EnumStatusConclusao.CONCLUIDO.name( ) );
    }
}