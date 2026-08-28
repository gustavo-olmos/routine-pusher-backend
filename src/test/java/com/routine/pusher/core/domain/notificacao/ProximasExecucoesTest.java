package com.routine.pusher.core.domain.notificacao;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cobre a projeção das próximas execuções: o que a saída da API antecipa a partir da recorrência,
 * sem tocar no lembrete nem no agendamento real.
 */
class ProximasExecucoesTest
{
    private static final int LIMITE = 5;

    private Lembrete lembreteComIntervalo( Duration intervalo, LocalDateTime dataInicio )
    {
        Recorrencia recorrencia = new Recorrencia( );
        recorrencia.setIntervaloHoras( (int) intervalo.toHours( ) );
        recorrencia.setIntervaloMinutos( intervalo.toMinutesPart( ) );

        Notificacao notificacao = new Notificacao( );
        notificacao.setDataInicio( dataInicio );

        return montar( recorrencia, notificacao );
    }

    private Lembrete montar( Recorrencia recorrencia, Notificacao notificacao )
    {
        Lembrete lembrete = new Lembrete( );
        lembrete.setRecorrencia( recorrencia );
        lembrete.setNotificacao( notificacao );
        return lembrete;
    }


    @Test
    @DisplayName("cron semanal projeta as próximas segundas, sempre no futuro e de 7 em 7 dias")
    void cron_projetaOcorrenciasConsecutivas( )
    {
        Recorrencia recorrencia = new Recorrencia( );
        recorrencia.setDiasDaSemana( List.of( EnumDiasDaSemana.SEGUNDA ) );

        Notificacao notificacao = new Notificacao( );
        notificacao.setHorario( LocalTime.of( 9, 0 ) );

        List<LocalDateTime> proximas = montar( recorrencia, notificacao ).calcularProximasExecucoes( LIMITE );

        assertThat( proximas ).hasSize( LIMITE )
                              .isSorted( )
                              .allMatch( data -> data.isAfter( LocalDateTime.now( ) ) )
                              .allMatch( data -> data.getDayOfWeek( ) == DayOfWeek.MONDAY )
                              .allMatch( data -> data.toLocalTime( ).equals( LocalTime.of( 9, 0 ) ) );

        assertThat( Duration.between( proximas.get( 0 ), proximas.get( 1 ) ) ).isEqualTo( Duration.ofDays( 7 ) );
    }

    @Test
    @DisplayName("intervalo projeta a série espaçada a partir do início")
    void intervalo_projetaSerieEspacada( )
    {
        LocalDateTime inicio = LocalDateTime.now( );

        List<LocalDateTime> proximas = lembreteComIntervalo( Duration.ofHours( 2 ), inicio )
                .calcularProximasExecucoes( LIMITE );

        assertThat( proximas ).hasSize( LIMITE );
        assertThat( proximas.get( 0 ) ).isEqualTo( inicio.plusHours( 2 ) );
        assertThat( proximas.get( LIMITE - 1 ) ).isEqualTo( inicio.plusHours( 10 ) );
    }

    @Test
    @DisplayName("início muito no passado não devolve ocorrências vencidas: salta para a próxima futura")
    void intervalo_iniciandoNoPassado_naoDevolveDatasVencidas( )
    {
        LocalDateTime inicio = LocalDateTime.now( ).minusHours( 10 );

        List<LocalDateTime> proximas = lembreteComIntervalo( Duration.ofHours( 2 ), inicio )
                .calcularProximasExecucoes( LIMITE );

        assertThat( proximas ).hasSize( LIMITE )
                              .allMatch( data -> data.isAfter( LocalDateTime.now( ) ) );
        assertThat( proximas.get( 0 ) ).isEqualTo( inicio.plusHours( 12 ) );
    }

    @Test
    @DisplayName("a série continua a partir do último disparo, não do início")
    void intervalo_partindoDaUltimaExecucao( )
    {
        LocalDateTime inicio = LocalDateTime.now( ).minusDays( 3 );
        LocalDateTime ultima = LocalDateTime.now( ).minusMinutes( 10 );

        Lembrete lembrete = lembreteComIntervalo( Duration.ofHours( 1 ), inicio );
        lembrete.getNotificacao( ).setUltimaExecucao( ultima );

        List<LocalDateTime> proximas = lembrete.calcularProximasExecucoes( LIMITE );

        assertThat( proximas.get( 0 ) ).isEqualTo( ultima.plusHours( 1 ) );
    }

    @Test
    @DisplayName("cota restante limita a projeção: não antecipa disparos que não vão acontecer")
    void cota_limitaAProjecao( )
    {
        Lembrete lembrete = lembreteComIntervalo( Duration.ofHours( 1 ), LocalDateTime.now( ) );
        lembrete.getRecorrencia( ).setQuantidade( 3 );

        assertThat( lembrete.calcularProximasExecucoes( LIMITE ) ).hasSize( 3 );
    }

    @Test
    @DisplayName("cota esgotada não projeta nada")
    void cotaEsgotada_projetaVazio( )
    {
        Lembrete lembrete = lembreteComIntervalo( Duration.ofHours( 1 ), LocalDateTime.now( ) );
        lembrete.getRecorrencia( ).setQuantidade( 0 );

        assertThat( lembrete.calcularProximasExecucoes( LIMITE ) ).isEmpty( );
    }

    @Test
    @DisplayName("validade encerra a série: nada além da data fim")
    void validade_encerraASerie( )
    {
        LocalDateTime inicio = LocalDateTime.now( );

        Lembrete lembrete = lembreteComIntervalo( Duration.ofHours( 1 ), inicio );
        lembrete.getNotificacao( ).setDataFim( inicio.plusHours( 2 ).plusMinutes( 30 ) );

        List<LocalDateTime> proximas = lembrete.calcularProximasExecucoes( LIMITE );

        assertThat( proximas ).containsExactly( inicio.plusHours( 1 ), inicio.plusHours( 2 ) );
    }

    @Test
    @DisplayName("datas específicas projetam apenas as futuras, em ordem")
    void datasEspecificadas_projetamApenasFuturas( )
    {
        LocalDateTime agora = LocalDateTime.now( );

        Notificacao notificacao = new Notificacao( );
        notificacao.setDatasEspecificadas( List.of( agora.plusDays( 2 ),
                                                    agora.minusDays( 1 ),
                                                    agora.plusDays( 1 ) ) );

        List<LocalDateTime> proximas = montar( null, notificacao ).calcularProximasExecucoes( LIMITE );

        assertThat( proximas ).containsExactly( agora.plusDays( 1 ), agora.plusDays( 2 ) );
    }

    @Test
    @DisplayName("sem recorrência reconhecível a projeção é vazia, não estoura")
    void semRecorrencia_projetaVazio( )
    {
        Lembrete lembrete = montar( new Recorrencia( ), new Notificacao( ) );

        assertThat( lembrete.calcularProximasExecucoes( LIMITE ) ).isEmpty( );
    }

    @Test
    @DisplayName("projeção não altera o lembrete: é leitura, roda a cada serialização")
    void projecao_naoAlteraOLembrete( )
    {
        LocalDateTime inicio = LocalDateTime.now( );
        Lembrete lembrete = lembreteComIntervalo( Duration.ofHours( 1 ), inicio );
        lembrete.getRecorrencia( ).setQuantidade( 3 );

        lembrete.calcularProximasExecucoes( LIMITE );

        assertThat( lembrete.getNotificacao( ).getProximaExecucao( ) ).isNull( );
        assertThat( lembrete.getNotificacao( ).getUltimaExecucao( ) ).isNull( );
        assertThat( lembrete.getRecorrencia( ).getQuantidade( ) ).isEqualTo( 3 );
    }
}
