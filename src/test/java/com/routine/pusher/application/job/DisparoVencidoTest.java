package com.routine.pusher.application.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Com job store persistente, tudo que venceu enquanto a aplicação esteve fora volta de uma vez no
 * boot. Estes casos fixam onde fica a fronteira entre "atrasou um pouco, ainda vale notificar" e
 * "isso é entulho de uma janela de indisponibilidade".
 */
class DisparoVencidoTest
{
    private final LembreteExecutorJob job = new LembreteExecutorJob( );

    private Date atras( long minutos )
    {
        return Date.from( Instant.now( ).minus( minutos, ChronoUnit.MINUTES ) );
    }

    @Test
    @DisplayName("disparo na hora não é vencido")
    void disparoNaHora_naoEhVencido( )
    {
        assertThat( job.disparoVencido( new Date( ) ) ).isFalse( );
    }

    @Test
    @DisplayName("atraso pequeno ainda notifica: reinício rápido não pode engolir o lembrete")
    void atrasoPequeno_aindaNotifica( )
    {
        assertThat( job.disparoVencido( atras( 2 ) ) ).isFalse( );
    }

    @Test
    @DisplayName("atraso de uma janela de indisponibilidade não notifica")
    void atrasoGrande_naoNotifica( )
    {
        assertThat( job.disparoVencido( atras( 30 ) ) ).isTrue( );
        assertThat( job.disparoVencido( atras( 60 * 24 ) ) ).isTrue( );
    }

    @Test
    @DisplayName("disparo futuro nunca é vencido")
    void disparoFuturo_naoEhVencido( )
    {
        assertThat( job.disparoVencido( atras( -10 ) ) ).isFalse( );
    }

    @Test
    @DisplayName("sem horário previsto, notifica: na dúvida, entrega")
    void semHorarioPrevisto_notifica( )
    {
        assertThat( job.disparoVencido( null ) ).isFalse( );
    }
}
