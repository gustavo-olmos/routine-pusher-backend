package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.core.domain.categoria.Categoria;
import com.routine.pusher.core.domain.feriado.port.FeriadoPort;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.enums.EnumStatusConclusao;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class Lembrete
{
    /**
     * Quantas execuções futuras a saída da API antecipa por lembrete.
     */
    public static final int LIMITE_PROXIMAS_EXECUCOES = 5;

    /**
     * Chave interna do banco. Existe porque uma PK sequencial mantém localidade no índice e deixa as
     * FKs em 8 bytes — UUID aleatório como PK fragmenta página de B-tree. Ela é carregada aqui só
     * para o mapeamento saber se é INSERT ou UPDATE: <b>nada consulta o lembrete por ela</b>, e ela
     * não aparece na saída da API.
     */
    private Long id;

    /**
     * Identidade pública, e a única pela qual o lembrete é procurado. Nasce aqui, no domínio, antes
     * de qualquer INSERT — então o lembrete já sabe quem é sem depender do banco, e a chave do job
     * no Quartz deixa de ser um número de sequência que uma recarga de base mudaria de lugar.
     */
    private UUID uuid;

    private LocalDateTime dataCriacao;

    private String titulo;
    private String descricao;
    private String status;
    private Categoria categoria;
    private Recorrencia recorrencia;
    private Notificacao notificacao;

    public Lembrete( )
    {
        this.uuid = UUID.randomUUID( );
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