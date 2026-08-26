package com.routine.pusher.core.domain.recorrencia;

import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.domain.recorrencia.cron.CronExpressionBuilder;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import com.routine.pusher.infrastructure.exceptions.StrategyException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recorrencia
{
    private Long id;

    private Integer quantidade;

    /*Sem CronExpression*/
    private Integer intervaloDias;
    private Integer intervaloHoras;
    private Integer intervaloMinutos;

    /*Com CronExpression*/
    private Integer posicaoDaSemanaNoMes;
    private List<Integer> diasFixosNoMes;
    private List<EnumDiasDaSemana> diasDaSemana;

    public Duration montarIntevalo( )
    {
        long dias = intervaloDias != null ? intervaloDias : 0;
        long horas = intervaloHoras != null ? intervaloHoras : 0;
        long minutos = intervaloMinutos != null ? intervaloMinutos : 0;

        return Duration.ofDays( dias ).plusHours( horas ).plusMinutes( minutos );
    }

    /**
     * {@code quantidade} nulo significa recorrência ilimitada. Quando definido, representa o número
     * de disparos ainda restantes — a contagem é decrementada a cada envio e persistida, de modo que
     * o limite é respeitado mesmo com o job recarregando o lembrete do banco a cada execução.
     */
    public boolean temQuantidadeRestante( )
    {
        return quantidade == null || quantidade > 0;
    }

    public void consumirQuantidade( )
    {
        if( quantidade != null && quantidade > 0 )
            quantidade = quantidade - 1;
    }

    public String montarCronExpression( Notificacao notificacao )
    {
        String cronExpression = "";

        if( diasDaSemana != null && !diasDaSemana.isEmpty( ) )
            cronExpression = CronExpressionBuilder.montarCronExprComDiasDaSemana( diasDaSemana );

        if( posicaoDaSemanaNoMes != null && posicaoDaSemanaNoMes > 0
                && diasDaSemana != null && diasDaSemana.size( ) == 1 ) {
            int codigoDia = diasDaSemana.get( 0 ).getCodigo( );
            cronExpression = CronExpressionBuilder.montarCronExprComPosicaoDaSemana( codigoDia , posicaoDaSemanaNoMes );
        }

        if( diasFixosNoMes != null && !diasFixosNoMes.isEmpty( ) )
            cronExpression = CronExpressionBuilder.montarCronExprComDiaFixo( diasFixosNoMes );

        // Sem componente de calendário não há expressão cron a montar. Devolver "" é contrato:
        // TriggerIlimitadoStrategy e TriggerValidadeStrategy testam justamente esse vazio para cair
        // no agendamento por intervalo. Exigir horário antes daqui quebrava todo lembrete de
        // intervalo, que legitimamente não tem horário fixo.
        if( cronExpression.isEmpty( ) )
            return cronExpression;

        LocalTime horario = notificacao.getHorario( );

        if( horario == null )
            throw new StrategyException( "Horário é obrigatório para montar a expressão cron" );

        int hora = horario.getHour( );
        int minuto = horario.getMinute( );
        int segundo = horario.getSecond( );

        return String.format("%d %d %d", segundo, minuto, hora ).concat( cronExpression );
    }
}
