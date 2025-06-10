package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.application.job.AgendadorJob;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.domain.categoria.Categoria;
import com.routine.pusher.core.enums.EnumStatusConclusao;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private List<String> metodoNotificacao;
    private Recorrencia recorrencia;

    private LocalTime horario;
    private LocalDateTime proxNotificacao;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private List<LocalDateTime> datasEspecificadas;

    public Lembrete( )
    {
        this.dataCriacao = LocalDateTime.now( );
        this.status = EnumStatusConclusao.PENDENTE.name( );
    }


    public void agendarLembrete( )
    {
        AgendadorJob.agendar( this );
    }

    public void concluirLembrete( )
    {
        this.setStatus( EnumStatusConclusao.CONCLUIDO.name( ) );
    }

    public LocalDateTime calcularProxNotificacaoComIntervalo( ) {
        setDataInicio( dataInicio.plus( recorrencia.montarIntevalo( ) ) );
        return dataInicio;
    }

    public String montarCronExpression( )
    {
        return String.format("%d %d %d", horario.getSecond( ), horario.getMinute( ), horario.getHour( ) )
                     .concat( recorrencia.montarCronExpression( ) );
    }
}
