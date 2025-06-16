package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.application.job.AgendadorJob;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.domain.categoria.Categoria;
import com.routine.pusher.core.enums.EnumStatusConclusao;
import lombok.Data;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
    private LocalDateTime proximaNotificacao;
    private LocalDateTime ultimaNotificacao;

    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private List<LocalDateTime> datasEspecificadas;

    public Lembrete( )
    {
        this.dataCriacao = LocalDateTime.now( );
        this.status = EnumStatusConclusao.PENDENTE.name( );
        calcularProximaNotificacao( );
    }


    public void agendarLembrete( )
    {
        AgendadorJob.agendar( this );
    }

    public void concluirLembrete( )
    {
        this.setStatus( EnumStatusConclusao.CONCLUIDO.name( ) );
    }

    public boolean aindaTemNotificacao( )
    {
        return Objects.nonNull( this.calcularProximaNotificacao( ) );
    }

    public LocalDateTime calcularProximaNotificacao( )
    {
        //TODO: implementar os casos em strategies
        if( !datasEspecificadas.isEmpty( ) ) {
            dataFim = datasEspecificadas.get( ( datasEspecificadas.size( ) - 1 ) );
            proximaNotificacao = datasEspecificadas.get( 0 );
        }

        Duration intervalo = recorrencia.montarIntevalo( );
        if( intervalo.isPositive( ) ) {
            proximaNotificacao = Objects.nonNull( ultimaNotificacao )
                ? ultimaNotificacao.plus( intervalo ) : dataInicio.plus( intervalo );
        }

        if( !recorrencia.getDiasDaSemana( ).isEmpty( ) ||
            !recorrencia.getDiasFixosNoMes( ).isEmpty( ) ||
            recorrencia.getPosicaoDaSemanaNoMes( ) > 0 ) {

            Date agora = new Date( );
            try {
                CronExpression cron = new CronExpression( montarCronExpression( ) );
                Date proxima = cron.getNextValidTimeAfter( agora );
                proximaNotificacao = proxima.toInstant( ).atZone( ZoneId.systemDefault( ) ).toLocalDateTime( );
            }
            catch ( ParseException e ) {
                throw new RuntimeException( e );
            }

        }

        return ( dataFim.isAfter( proximaNotificacao ) ) ? proximaNotificacao : null;
    }

    public String montarCronExpression( )
    {
        return String.format("%d %d %d", horario.getSecond( ), horario.getMinute( ), horario.getHour( ) )
                     .concat( recorrencia.montarCronExpression( ) );
    }
}