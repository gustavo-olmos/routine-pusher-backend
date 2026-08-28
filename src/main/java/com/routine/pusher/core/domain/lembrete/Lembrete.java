package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.core.domain.categoria.Categoria;
import com.routine.pusher.core.domain.feriado.port.FeriadoPort;
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


    public void setExecucao( FeriadoPort feriados )
    {
        if( notificacao.aindaTemNotificacao( this, feriados ) )
            notificacao.setProximaExecucao( notificacao.calcularProximaNotificacao( this, feriados ) );
    }

    public List<LocalDateTime> calcularProximasExecucoes( int limite )
    {
        return this.calcularProximasExecucoes( limite, null );
    }

    /**
     * {@code feriados} nulo significa "sem restrição de calendário", e é o comportamento desejado
     * quando não há fonte de feriados disponível: a série segue como a recorrência calculou. Falhar
     * aberto é deliberado — notificação a mais incomoda, notificação a menos quebra o produto.
     */
    public List<LocalDateTime> calcularProximasExecucoes( int limite, FeriadoPort feriados )
    {
        if( notificacao == null ) return List.of( );

        return notificacao.calcularProximasNotificacoes( this, limite, feriados );
    }

    public void concluirLembrete( )
    {
        this.setStatus( EnumStatusConclusao.CONCLUIDO.name( ) );
    }
}