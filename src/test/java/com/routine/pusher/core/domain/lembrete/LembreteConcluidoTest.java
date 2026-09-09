package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import com.routine.pusher.core.enums.EnumStatusConclusao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Concluir um lembrete cancela o agendamento — logo ele não dispara mais. Estes testes prendem o
 * outro lado dessa verdade: a saída da API precisa parar de anunciar execuções.
 * <p>
 * O bug que motivou a classe passava despercebido justamente porque a projeção é calculada e o
 * status é persistido: nada quebrava, a API só respondia cinco datas que jamais aconteceriam.
 */
class LembreteConcluidoTest
{
    private static final int LIMITE = 5;

    private Lembrete lembreteSemanal( )
    {
        Recorrencia recorrencia = new Recorrencia( );
        recorrencia.setDiasDaSemana( List.of( EnumDiasDaSemana.SEGUNDA ) );

        Notificacao notificacao = new Notificacao( );
        notificacao.setHorario( LocalTime.of( 8, 0 ) );

        Lembrete lembrete = new Lembrete( );
        lembrete.setRecorrencia( recorrencia );
        lembrete.setNotificacao( notificacao );
        lembrete.setExecucao( null );

        return lembrete;
    }


    @Test
    @DisplayName("lembrete pendente projeta as próximas execuções normalmente")
    void pendente_projetaExecucoes( )
    {
        assertThat( lembreteSemanal( ).calcularProximasExecucoes( LIMITE ) ).hasSize( LIMITE );
    }

    @Test
    @DisplayName("lembrete concluído não projeta execução nenhuma")
    void concluido_naoProjetaExecucoes( )
    {
        Lembrete lembrete = lembreteSemanal( );
        lembrete.concluirLembrete( );

        assertThat( lembrete.calcularProximasExecucoes( LIMITE ) ).isEmpty( );
    }

    @Test
    @DisplayName("concluir zera a próxima execução, que deixaria o campo apontando para um disparo que não existe")
    void concluir_zeraProximaExecucao( )
    {
        Lembrete lembrete = lembreteSemanal( );
        assertThat( lembrete.getNotificacao( ).getProximaExecucao( ) ).isNotNull( );

        lembrete.concluirLembrete( );

        assertThat( lembrete.getNotificacao( ).getProximaExecucao( ) ).isNull( );
    }

    @Test
    @DisplayName("reabrir devolve o lembrete à projeção, porque a edição rearma o disparo")
    void reabrir_voltaAProjetar( )
    {
        Lembrete lembrete = lembreteSemanal( );
        lembrete.concluirLembrete( );

        lembrete.reabrirSeConcluido( );
        lembrete.setExecucao( null );

        assertThat( lembrete.getStatus( ) ).isEqualTo( EnumStatusConclusao.PENDENTE.name( ) );
        assertThat( lembrete.calcularProximasExecucoes( LIMITE ) ).hasSize( LIMITE );
    }

    @Test
    @DisplayName("reabrir não mexe em lembrete que já está pendente")
    void reabrir_naoAlteraPendente( )
    {
        Lembrete lembrete = lembreteSemanal( );

        lembrete.reabrirSeConcluido( );

        assertThat( lembrete.getStatus( ) ).isEqualTo( EnumStatusConclusao.PENDENTE.name( ) );
    }
}
