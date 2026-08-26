package com.routine.pusher.core.domain.recorrencia;

import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import com.routine.pusher.infrastructure.exceptions.StrategyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecorrenciaTest
{
    private Recorrencia comIntervalo( Integer dias, Integer horas, Integer minutos )
    {
        Recorrencia recorrencia = new Recorrencia( );
        recorrencia.setIntervaloDias( dias );
        recorrencia.setIntervaloHoras( horas );
        recorrencia.setIntervaloMinutos( minutos );
        return recorrencia;
    }

    @Test
    @DisplayName("montarIntevalo soma dias, horas e minutos (sem duplicar horas nem ignorar minutos)")
    void montarIntevalo_deveSomarDiasHorasEMinutos( )
    {
        Duration intervalo = comIntervalo( 1, 2, 30 ).montarIntevalo( );

        assertThat( intervalo ).isEqualTo( Duration.ofDays( 1 ).plusHours( 2 ).plusMinutes( 30 ) );
    }

    @Test
    @DisplayName("montarIntevalo trata campos nulos como zero, sem NPE")
    void montarIntevalo_deveTratarNulosComoZero( )
    {
        Duration intervalo = comIntervalo( null, 5, null ).montarIntevalo( );

        assertThat( intervalo ).isEqualTo( Duration.ofHours( 5 ) );
    }

    @Test
    @DisplayName("quantidade nula representa recorrência ilimitada")
    void quantidadeNula_eIlimitada( )
    {
        Recorrencia recorrencia = new Recorrencia( );
        recorrencia.setQuantidade( null );

        recorrencia.consumirQuantidade( );

        assertThat( recorrencia.temQuantidadeRestante( ) ).isTrue( );
        assertThat( recorrencia.getQuantidade( ) ).isNull( );
    }

    @Test
    @DisplayName("consumir esgota a cota após exatamente 'quantidade' disparos")
    void consumir_esgotaAposQuantidadeDisparos( )
    {
        Recorrencia recorrencia = new Recorrencia( );
        recorrencia.setQuantidade( 2 );

        assertThat( recorrencia.temQuantidadeRestante( ) ).isTrue( );

        recorrencia.consumirQuantidade( );                       // 1º disparo -> resta 1
        assertThat( recorrencia.temQuantidadeRestante( ) ).isTrue( );

        recorrencia.consumirQuantidade( );                       // 2º disparo -> resta 0
        assertThat( recorrencia.temQuantidadeRestante( ) ).isFalse( );

        recorrencia.consumirQuantidade( );                       // não vai abaixo de zero
        assertThat( recorrencia.getQuantidade( ) ).isZero( );
    }

    /**
     * Contrato de que TriggerIlimitadoStrategy e TriggerValidadeStrategy dependem: expressão vazia
     * significa "não é recorrência de calendário, agende por intervalo". Quando isto passou a lançar
     * por falta de horário, todo lembrete de intervalo parou de ser criado.
     */
    @Test
    @DisplayName("montarCronExpression devolve vazio quando não há componente de calendário")
    void montarCronExpression_semCalendario_deveDevolverVazio( )
    {
        Recorrencia soIntervalo = comIntervalo( null, null, 30 );

        assertThat( soIntervalo.montarCronExpression( new Notificacao( ) ) ).isEmpty( );
    }

    @Test
    @DisplayName("montarCronExpression exige horário apenas quando há recorrência de calendário")
    void montarCronExpression_comCalendarioSemHorario_deveLancar( )
    {
        Recorrencia porDiaDaSemana = new Recorrencia( );
        porDiaDaSemana.setDiasDaSemana( List.of( EnumDiasDaSemana.SEGUNDA ) );

        assertThatThrownBy( () -> porDiaDaSemana.montarCronExpression( new Notificacao( ) ) )
                .isInstanceOf( StrategyException.class );
    }

    @Test
    @DisplayName("montarCronExpression prefixa o horário na expressão de calendário")
    void montarCronExpression_comCalendarioEHorario_deveMontar( )
    {
        Recorrencia porDiaDaSemana = new Recorrencia( );
        porDiaDaSemana.setDiasDaSemana( List.of( EnumDiasDaSemana.SEGUNDA ) );

        Notificacao notificacao = new Notificacao( );
        notificacao.setHorario( LocalTime.of( 9, 30 ) );

        assertThat( porDiaDaSemana.montarCronExpression( notificacao ) ).startsWith( "0 30 9" );
    }
}
