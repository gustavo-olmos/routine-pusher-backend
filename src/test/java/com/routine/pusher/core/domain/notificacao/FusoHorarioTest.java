package com.routine.pusher.core.domain.notificacao;

import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Caracteriza — não corrige — o contrato de fuso horário da aplicação.
 *
 * <p>Toda data trafega e é persistida como {@code LocalDateTime}: hora de parede, sem offset. Quem
 * decide a que instante ela corresponde é o fuso do servidor, via {@code LocalDateTime.now()} e
 * {@code ZoneId.systemDefault()} nos triggers. Ou seja: o back não é neutro em relação ao fuso, ele
 * assume o próprio. Enquanto servidor e usuário estiverem no mesmo fuso — o caso do desenvolvimento
 * local — nada aparece; num host em UTC, aparece.</p>
 *
 * <p>Estes testes fixam esse comportamento para que uma eventual mudança de contrato seja uma
 * decisão explícita, e não uma surpresa em produção.</p>
 *
 * <p><b>O risco se concretizou.</b> Ao rodar a imagem em 2026-08-28, o container subiu em UTC e os
 * horários saíram três horas adiantados — exatamente o que os dois casos marcados como RISCO DE
 * DEPLOY descrevem. A mitigação foi alinhar o relógio do servidor ao do público
 * ({@code ENV TZ=America/Sao_Paulo} no Dockerfile), o que torna verdadeira a premissa que este
 * contrato já assumia. Continua sendo mitigação, não correção: a correção é cada lembrete guardar
 * o fuso de quem o criou.</p>
 */
class FusoHorarioTest
{
    private static final ZoneId SAO_PAULO = ZoneId.of( "America/Sao_Paulo" );

    private final TimeZone fusoOriginal = TimeZone.getDefault( );

    @AfterEach
    void restaurarFuso( )
    {
        TimeZone.setDefault( fusoOriginal );
    }

    private void servidorEm( ZoneId zona )
    {
        TimeZone.setDefault( TimeZone.getTimeZone( zona ) );
    }

    /** O que um front em São Paulo manda ao pedir "daqui a uma hora": hora de parede, sem offset. */
    private LocalDateTime enviadoPeloFrontDeSaoPaulo( )
    {
        return ZonedDateTime.now( SAO_PAULO ).plusHours( 1 ).toLocalDateTime( );
    }

    private Lembrete lembreteComData( LocalDateTime data )
    {
        Notificacao notificacao = new Notificacao( );
        notificacao.setDatasEspecificadas( List.of( data ) );

        Lembrete lembrete = new Lembrete( );
        lembrete.setNotificacao( notificacao );
        return lembrete;
    }


    @Test
    @DisplayName("servidor no mesmo fuso do usuário: a data enviada é futura, como esperado")
    void servidorNoMesmoFuso_dataEhFutura( )
    {
        LocalDateTime doFront = enviadoPeloFrontDeSaoPaulo( );

        servidorEm( SAO_PAULO );

        assertThat( lembreteComData( doFront ).calcularProximasExecucoes( 5 ) )
                .containsExactly( doFront );
    }

    @Test
    @DisplayName("RISCO DE DEPLOY: servidor em UTC trata a mesma data como vencida e a descarta")
    void servidorEmUtc_descartaDataQueOFrontConsideraFutura( )
    {
        LocalDateTime doFront = enviadoPeloFrontDeSaoPaulo( );

        servidorEm( ZoneOffset.UTC );

        // O servidor compara a hora de parede recebida com o próprio 'agora', que em UTC está 3h à
        // frente. A data some da projeção — e o agendamento lança "Não há datas futuras para agendar".
        assertThat( lembreteComData( doFront ).calcularProximasExecucoes( 5 ) )
                .as( "hospedar em UTC muda o significado da data que o front mandou" )
                .isEmpty( );
    }

    @Test
    @DisplayName("RISCO DE DEPLOY: cron das 9h é 9h do servidor, ou seja 6h para o usuário em São Paulo")
    void cron_disparaNoFusoDoServidor( )
    {
        Recorrencia recorrencia = new Recorrencia( );
        recorrencia.setDiasDaSemana( List.of( EnumDiasDaSemana.SEGUNDA, EnumDiasDaSemana.TERCA,
                EnumDiasDaSemana.QUARTA, EnumDiasDaSemana.QUINTA, EnumDiasDaSemana.SEXTA ) );

        Notificacao notificacao = new Notificacao( );
        notificacao.setHorario( LocalTime.of( 9, 0 ) );

        Lembrete lembrete = new Lembrete( );
        lembrete.setRecorrencia( recorrencia );
        lembrete.setNotificacao( notificacao );

        servidorEm( ZoneOffset.UTC );

        LocalDateTime proxima = lembrete.calcularProximasExecucoes( 1 ).get( 0 );

        assertThat( proxima.toLocalTime( ) )
                .as( "a projeção devolve 09:00 na hora de parede do servidor" )
                .isEqualTo( LocalTime.of( 9, 0 ) );

        LocalTime paraOUsuario = proxima.atZone( ZoneOffset.UTC )
                                        .withZoneSameInstant( SAO_PAULO )
                                        .toLocalTime( );

        assertThat( paraOUsuario )
                .as( "mas o instante agendado chega ao usuário em São Paulo 3h mais cedo" )
                .isEqualTo( LocalTime.of( 6, 0 ) );
    }
}
