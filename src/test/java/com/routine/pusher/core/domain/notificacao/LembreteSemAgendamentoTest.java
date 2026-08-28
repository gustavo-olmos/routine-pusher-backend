package com.routine.pusher.core.domain.notificacao;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Um lembrete sem nenhuma regra de quando disparar não tem strategy — e antes deste guard isso
 * estourava NullPointerException no caminho do agendamento, virando 500. A projeção já tratava o
 * caso; o agendamento não. Quem expôs foi o chat de IA com uma frase sem sentido, mas um POST
 * humano sem recorrência e sem data cai no mesmo lugar.
 */
class LembreteSemAgendamentoTest
{
    private Lembrete lembreteSemRegra( )
    {
        Lembrete lembrete = new Lembrete( );
        lembrete.setTitulo( "Sem quando" );

        Notificacao notificacao = new Notificacao( );
        notificacao.setMetodo( List.of( "pop-up" ) );
        lembrete.setNotificacao( notificacao );

        return lembrete;
    }

    @Test
    @DisplayName("sem regra de disparo não estoura: responde 'não há próxima execução'")
    void semRegra_naoEstoura( )
    {
        Lembrete lembrete = lembreteSemRegra( );

        assertThatCode( ( ) -> lembrete.getNotificacao( ).calcularProximaNotificacao( lembrete ) )
                .doesNotThrowAnyException( );

        assertThat( lembrete.getNotificacao( ).calcularProximaNotificacao( lembrete ) ).isNull( );
    }

    @Test
    @DisplayName("e o lembrete se declara sem notificação pendente, em vez de mentir")
    void semRegra_naoTemNotificacao( )
    {
        Lembrete lembrete = lembreteSemRegra( );

        assertThat( lembrete.getNotificacao( ).aindaTemNotificacao( lembrete ) ).isFalse( );
    }

    @Test
    @DisplayName("recorrência vazia é o mesmo caso: intervalo zero não agenda nada")
    void recorrenciaVazia_naoEstoura( )
    {
        Lembrete lembrete = lembreteSemRegra( );
        lembrete.setRecorrencia( new Recorrencia( ) );

        assertThat( lembrete.getNotificacao( ).calcularProximaNotificacao( lembrete ) ).isNull( );
        assertThat( lembrete.calcularProximasExecucoes( 5 ) ).isEmpty( );
    }

    @Test
    @DisplayName("com regra válida volta a calcular normalmente")
    void comRegra_calcula( )
    {
        Lembrete lembrete = lembreteSemRegra( );
        lembrete.getNotificacao( ).setDatasEspecificadas(
                List.of( LocalDateTime.now( ).plusHours( 1 ) ) );

        assertThat( lembrete.getNotificacao( ).calcularProximaNotificacao( lembrete ) ).isNotNull( );
    }
}
