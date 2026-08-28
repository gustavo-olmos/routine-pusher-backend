package com.routine.pusher.core.domain.notificacao;

import com.routine.pusher.core.domain.feriado.adapter.FeriadoNacionalBrasilAdapter;
import com.routine.pusher.core.domain.feriado.port.FeriadoPort;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import com.routine.pusher.core.enums.EnumPoliticaDiaUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * As datas fixas dos casos ficam em 2032 e 2035 de propósito: são feriados nacionais em dias de
 * semana conhecidos (07/09/2032 numa terça, 07/09/2035 numa sexta), o que deixa o teste
 * determinístico independentemente de quando ele roda.
 */
class PoliticaDiaUtilTest
{
    private static final FeriadoPort FERIADOS = new FeriadoNacionalBrasilAdapter( );

    private static final LocalTime NOVE = LocalTime.of( 9, 0 );

    private LocalDateTime em( int ano, int mes, int dia )
    {
        return LocalDateTime.of( ano, mes, dia, 9, 0 );
    }

    private Lembrete diasUteisComPolitica( EnumPoliticaDiaUtil politica )
    {
        Recorrencia recorrencia = new Recorrencia( );
        recorrencia.setDiasDaSemana( List.of( EnumDiasDaSemana.SEGUNDA, EnumDiasDaSemana.TERCA,
                EnumDiasDaSemana.QUARTA, EnumDiasDaSemana.QUINTA, EnumDiasDaSemana.SEXTA ) );
        recorrencia.setPoliticaDiaUtil( politica );

        Notificacao notificacao = new Notificacao( );
        notificacao.setHorario( NOVE );

        Lembrete lembrete = new Lembrete( );
        lembrete.setId( 1L );
        lembrete.setRecorrencia( recorrencia );
        lembrete.setNotificacao( notificacao );
        return lembrete;
    }


    @Test
    @DisplayName("PULAR descarta a ocorrência que cai no feriado")
    void pular_descartaOFeriado( )
    {
        List<LocalDateTime> ocorrencias = List.of( em( 2032, 9, 6 ), em( 2032, 9, 7 ), em( 2032, 9, 8 ) );

        assertThat( EnumPoliticaDiaUtil.PULAR.aplicar( ocorrencias, FERIADOS ) )
                .containsExactly( em( 2032, 9, 6 ), em( 2032, 9, 8 ) );
    }

    @Test
    @DisplayName("ADIAR leva a terça-feriado para a quarta seguinte")
    void adiar_levaParaOProximoDiaUtil( )
    {
        assertThat( EnumPoliticaDiaUtil.ADIAR.aplicar( List.of( em( 2032, 9, 7 ) ), FERIADOS ) )
                .containsExactly( em( 2032, 9, 8 ) );
    }

    @Test
    @DisplayName("ADIAR atravessa o fim de semana: sexta-feriado vira segunda")
    void adiar_atravessaOFimDeSemana( )
    {
        // 07/09/2035 é sexta-feira. Adiar precisa passar por sábado e domingo até 10/09, segunda.
        assertThat( EnumPoliticaDiaUtil.ADIAR.aplicar( List.of( em( 2035, 9, 7 ) ), FERIADOS ) )
                .containsExactly( em( 2035, 9, 10 ) );
    }

    @Test
    @DisplayName("ANTECIPAR volta para o dia útil anterior — o caso do boleto")
    void antecipar_voltaParaODiaUtilAnterior( )
    {
        assertThat( EnumPoliticaDiaUtil.ANTECIPAR.aplicar( List.of( em( 2032, 9, 7 ) ), FERIADOS ) )
                .containsExactly( em( 2032, 9, 6 ) );
    }

    @Test
    @DisplayName("duas ocorrências que caem no mesmo dia útil não viram horário repetido")
    void adiar_naoRepeteHorario( )
    {
        // Sábado 04/09/2032 e domingo 05/09 adiam para a mesma segunda 06/09.
        List<LocalDateTime> ocorrencias = List.of( em( 2032, 9, 4 ), em( 2032, 9, 5 ) );

        assertThat( EnumPoliticaDiaUtil.ADIAR.aplicar( ocorrencias, FERIADOS ) )
                .containsExactly( em( 2032, 9, 6 ) );
    }

    @Test
    @DisplayName("IGNORAR devolve a série intacta")
    void ignorar_naoMexeNaSerie( )
    {
        List<LocalDateTime> ocorrencias = List.of( em( 2032, 9, 6 ), em( 2032, 9, 7 ) );

        assertThat( EnumPoliticaDiaUtil.IGNORAR.aplicar( ocorrencias, FERIADOS ) )
                .isEqualTo( ocorrencias );
    }

    @Test
    @DisplayName("a projeção de dias úteis com PULAR nunca cai em fim de semana nem feriado")
    void projecao_comPular_naoCaiEmDiaNaoUtil( )
    {
        List<LocalDateTime> proximas = diasUteisComPolitica( EnumPoliticaDiaUtil.PULAR )
                .calcularProximasExecucoes( 10, FERIADOS );

        assertThat( proximas ).isNotEmpty( )
                              .isSorted( )
                              .noneMatch( data -> data.getDayOfWeek( ) == DayOfWeek.SATURDAY )
                              .noneMatch( data -> data.getDayOfWeek( ) == DayOfWeek.SUNDAY )
                              .noneMatch( data -> FERIADOS.ehFeriado( data.toLocalDate( ) ) );
    }

    @Test
    @DisplayName("sem política a projeção volta a cair em feriado")
    void projecao_semPolitica_caiEmFeriado( )
    {
        Lembrete semPolitica = diasUteisComPolitica( EnumPoliticaDiaUtil.IGNORAR );

        List<LocalDateTime> proximas = semPolitica.calcularProximasExecucoes( 250, FERIADOS );

        assertThat( proximas ).as( "sem política, o cron ignora o calendário e projeta o feriado" )
                              .anyMatch( data -> FERIADOS.ehFeriado( data.toLocalDate( ) ) );
    }

    @Test
    @DisplayName("o disparo agendado é o mesmo que a prévia mostra: a política vale nos dois caminhos")
    void agendamento_concordaComAPrevia( )
    {
        Lembrete lembrete = diasUteisComPolitica( EnumPoliticaDiaUtil.PULAR );

        lembrete.setExecucao( FERIADOS );

        assertThat( lembrete.getNotificacao( ).getProximaExecucao( ) )
                .isEqualTo( lembrete.calcularProximasExecucoes( 1, FERIADOS ).get( 0 ) );
    }

    @Test
    @DisplayName("sem fonte de feriados a série segue: falha aberta, não em silêncio")
    void semFonteDeFeriados_serieSegue( )
    {
        List<LocalDateTime> proximas = diasUteisComPolitica( EnumPoliticaDiaUtil.PULAR )
                .calcularProximasExecucoes( 5, null );

        assertThat( proximas ).hasSize( 5 );
    }
}
