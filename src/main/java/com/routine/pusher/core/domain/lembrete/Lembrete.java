package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.application.job.AgendadorJob;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.domain.categoria.Categoria;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import com.routine.pusher.core.enums.EnumStatusConclusao;
import com.routine.pusher.infrastructure.common.helper.CronExpessionBuilder;
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
    private List<LocalDateTime> momentosEspecificados;
    private LocalDateTime momento;
    private List<String> metodoNotificacao;
    private LocalDateTime aPartirDe;

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

    public String montaCronExpression( )
    {
        String cronExpression = "";

        List<EnumDiasDaSemana> diasSemana = recorrencia.getDiasDaSemana( );
        if( !diasSemana.isEmpty( ) )
            cronExpression = CronExpessionBuilder.montarCronExprComDiasDaSemana( momento, diasSemana );

        int posicao = recorrencia.getPosicaoDaSemanaNoMes( );
        if( posicao > 0 && diasSemana.size( ) == 1 ) {
            int codigoDia = diasSemana.get( 0 ).getCodigo( );
            cronExpression = CronExpessionBuilder.montarCronExprComPosicaoDaSemana( momento, codigoDia , posicao );
        }

        List<Integer> diasFixosNoMes = recorrencia.getDiasFixosNoMes();
        if( !diasFixosNoMes.isEmpty( ) )
            cronExpression = CronExpessionBuilder.montarCronExprComDiaFixo( momento, diasFixosNoMes );

        return cronExpression;
    }

    public LocalDateTime calcularProxNotificacaoComIntervalo( ) {
        setAPartirDe( aPartirDe.plus( recorrencia.montarIntevalo( ) ) );
        return aPartirDe;
    }
}
