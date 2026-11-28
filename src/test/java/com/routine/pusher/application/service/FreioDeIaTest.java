package com.routine.pusher.application.service;

import com.routine.pusher.infrastructure.exceptions.IaIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * O freio global: balde de fichas para rajadas e disjuntor depois que o provedor recusa. O relógio é
 * de mentira para o teste controlar o tempo sem dormir.
 */
class FreioDeIaTest
{
    private RelogioDeTeste relogio;
    private FreioDeIa freio;

    @BeforeEach
    void montar( )
    {
        relogio = new RelogioDeTeste( Instant.parse( "2026-11-28T12:00:00Z" ) );
        freio = new FreioDeIa( relogio, 3, Duration.ofSeconds( 60 ) );
    }

    @Test
    @DisplayName("rajada além do balde espera, e o Retry-After diz quanto")
    void rajada_alemDoBalde_recusaComEspera( )
    {
        freio.autorizar( );
        freio.autorizar( );
        freio.autorizar( );

        assertThatThrownBy( freio::autorizar )
                .isInstanceOfSatisfying( IaIndisponivelException.class, e -> {
                    assertThat( e.recusaDoProvedor( ) ).isFalse( );
                    assertThat( e.tenteEm( ) ).isEqualTo( Duration.ofSeconds( 20 ) );
                } );
    }

    @Test
    @DisplayName("as fichas voltam com o tempo")
    void fichas_voltamComOTempo( )
    {
        freio.autorizar( );
        freio.autorizar( );
        freio.autorizar( );

        relogio.andar( Duration.ofSeconds( 20 ) );

        assertThatCode( freio::autorizar ).doesNotThrowAnyException( );
    }

    @Test
    @DisplayName("provedor recusou por limite: ninguém chama durante a pausa")
    void recusaDoProvedor_pausaAsChamadas( )
    {
        freio.registrar( new IaIndisponivelException( "limite", Duration.ofSeconds( 60 ), true ) );

        relogio.andar( Duration.ofSeconds( 45 ) );
        assertThatThrownBy( freio::autorizar )
                .isInstanceOfSatisfying( IaIndisponivelException.class,
                        e -> assertThat( e.tenteEm( ) ).isEqualTo( Duration.ofSeconds( 15 ) ) );

        relogio.andar( Duration.ofSeconds( 15 ) );
        assertThatCode( freio::autorizar ).doesNotThrowAnyException( );
    }

    @Test
    @DisplayName("falha de rede pontual não pausa ninguém")
    void falhaDeRede_naoPausa( )
    {
        freio.registrar( new IaIndisponivelException( "rede", Duration.ofSeconds( 30 ), false ) );

        assertThatCode( freio::autorizar ).doesNotThrowAnyException( );
    }

    private static final class RelogioDeTeste extends Clock
    {
        private Instant agora;

        RelogioDeTeste( Instant inicio )
        {
            this.agora = inicio;
        }

        void andar( Duration quanto )
        {
            agora = agora.plus( quanto );
        }

        @Override
        public ZoneId getZone( )
        {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone( ZoneId zone )
        {
            return this;
        }

        @Override
        public Instant instant( )
        {
            return agora;
        }
    }
}
