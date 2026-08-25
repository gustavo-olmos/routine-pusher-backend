package com.routine.pusher.core.domain.recorrencia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

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
}
