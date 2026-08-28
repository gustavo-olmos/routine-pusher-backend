package com.routine.pusher.core.domain.notificacao;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * "Dias úteis" não é um conceito próprio do domínio: é a lista dos cinco dias da semana. Estes testes
 * verificam que essa combinação realmente cadastra e agenda, já que nenhum teste cobria mais de três
 * dias simultâneos.
 */
class DiasUteisTest
{
    private static final List<EnumDiasDaSemana> DIAS_UTEIS =
            List.of( EnumDiasDaSemana.SEGUNDA, EnumDiasDaSemana.TERCA, EnumDiasDaSemana.QUARTA,
                     EnumDiasDaSemana.QUINTA, EnumDiasDaSemana.SEXTA );

    private Lembrete lembreteDiasUteis( LocalTime horario )
    {
        Recorrencia recorrencia = new Recorrencia( );
        recorrencia.setDiasDaSemana( DIAS_UTEIS );

        Notificacao notificacao = new Notificacao( );
        notificacao.setHorario( horario );

        Lembrete lembrete = new Lembrete( );
        lembrete.setRecorrencia( recorrencia );
        lembrete.setNotificacao( notificacao );
        return lembrete;
    }

    @Test
    @DisplayName("os cinco dias úteis viram uma expressão cron válida")
    void diasUteis_montamCronValida( )
    {
        Recorrencia recorrencia = new Recorrencia( );
        recorrencia.setDiasDaSemana( DIAS_UTEIS );

        Notificacao notificacao = new Notificacao( );
        notificacao.setHorario( LocalTime.of( 9, 0 ) );

        String cron = recorrencia.montarCronExpression( notificacao );

        assertThat( cron ).isEqualTo( "0 0 9 ? * MON,TUE,WED,THU,FRI" );
        assertThat( CronExpression.isValidExpression( cron ) ).isTrue( );
    }

    @Test
    @DisplayName("a projeção cobre uma semana útil inteira, sem sábado nem domingo")
    void diasUteis_projetamCincoDiasSemFimDeSemana( )
    {
        List<LocalDateTime> proximas = lembreteDiasUteis( LocalTime.of( 9, 0 ) )
                .calcularProximasExecucoes( 5 );

        assertThat( proximas ).hasSize( 5 )
                              .isSorted( )
                              .allMatch( data -> data.toLocalTime( ).equals( LocalTime.of( 9, 0 ) ) )
                              .noneMatch( data -> data.getDayOfWeek( ) == DayOfWeek.SATURDAY )
                              .noneMatch( data -> data.getDayOfWeek( ) == DayOfWeek.SUNDAY );

        assertThat( proximas ).extracting( LocalDateTime::getDayOfWeek )
                              .containsExactlyInAnyOrder( DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                                      DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY );
    }

    @Test
    @DisplayName("a sexta emenda na segunda: o fim de semana é pulado, não adiantado")
    void diasUteis_pulamOFimDeSemana( )
    {
        List<LocalDateTime> proximas = lembreteDiasUteis( LocalTime.of( 9, 0 ) )
                .calcularProximasExecucoes( 5 );

        LocalDateTime sexta = proximas.stream( )
                .filter( data -> data.getDayOfWeek( ) == DayOfWeek.FRIDAY )
                .findFirst( )
                .orElseThrow( );

        LocalDateTime seguinte = proximas.stream( )
                .filter( data -> data.isAfter( sexta ) )
                .findFirst( )
                .orElse( null );

        if( seguinte != null ) {
            assertThat( seguinte.getDayOfWeek( ) ).isEqualTo( DayOfWeek.MONDAY );
            assertThat( seguinte.toLocalDate( ) ).isEqualTo( sexta.toLocalDate( ).plusDays( 3 ) );
        }
    }
}
