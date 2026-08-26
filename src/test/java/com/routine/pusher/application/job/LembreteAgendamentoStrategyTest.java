package com.routine.pusher.application.job;

import com.routine.pusher.application.service.LembreteService;
import com.routine.pusher.core.domain.categoria.CategoriaEntity;
import com.routine.pusher.core.domain.categoria.CategoriaRepository;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.notificacao.dto.NotificacaoInputDTO;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaInputDTO;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cobre as combinações de recorrência do motor que até aqui não tinham teste nenhum: o único teste de
 * integração existente exercita apenas {@code datasEspecificadas}. Foi essa lacuna que deixou passar
 * a regressão em {@code montarCronExpression}, onde exigir horário cedo demais quebrava todo lembrete
 * de intervalo.
 *
 * <p>Cada caso vai até o agendamento real no Quartz: se a strategy escolhida não conseguir montar o
 * trigger, {@code adicionar} lança e o teste falha.</p>
 */
@SpringBootTest
class LembreteAgendamentoStrategyTest
{
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private LembreteService lembreteService;

    private Long categoriaId( String cor, int fatorOrdem )
    {
        CategoriaEntity categoria = new CategoriaEntity( );
        categoria.setNome( "Categoria " + fatorOrdem );
        categoria.setCor( cor );
        categoria.setFatorOrdem( fatorOrdem );

        return categoriaRepository.save( categoria ).getId( );
    }

    @Test
    @DisplayName("intervalo sem horário e sem limite agenda (regressão do montarCronExpression)")
    void intervaloIlimitado_deveAgendar( )
    {
        LembreteOutputDTO criado = lembreteService.adicionar( new LembreteInputDTO(
                "Beber água", "de 2 em 2 minutos", categoriaId( "#E53935", 101 ),
                new RecorrenciaInputDTO( null, null, null, 2, null, null, null ),
                new NotificacaoInputDTO( List.of( "pop-up" ), null, LocalDateTime.now( ), null, null ) ) );

        assertThat( criado.notificacao( ).proximaExecucao( ) )
                .as( "intervalo de 2 min deve render uma próxima execução no futuro" )
                .isAfter( LocalDateTime.now( ) );
    }

    @Test
    @DisplayName("intervalo com quantidade agenda e preserva a cota")
    void intervaloPorQuantidade_deveAgendar( )
    {
        LembreteOutputDTO criado = lembreteService.adicionar( new LembreteInputDTO(
                "Alongar", "3 em 3 minutos, 2 disparos", categoriaId( "#FB8C00", 102 ),
                new RecorrenciaInputDTO( 2, null, null, 3, null, null, null ),
                new NotificacaoInputDTO( List.of( "pop-up" ), null, LocalDateTime.now( ), null, null ) ) );

        assertThat( criado.recorrencia( ).quantidade( ) ).isEqualTo( 2 );
        assertThat( criado.notificacao( ).proximaExecucao( ) ).isAfter( LocalDateTime.now( ) );
    }

    @Test
    @DisplayName("intervalo com validade agenda dentro da janela")
    void intervaloComValidade_deveAgendar( )
    {
        LocalDateTime agora = LocalDateTime.now( );

        LembreteOutputDTO criado = lembreteService.adicionar( new LembreteInputDTO(
                "Pausa para o café", "1 em 1 minuto, expira em 10", categoriaId( "#1E88E5", 103 ),
                new RecorrenciaInputDTO( null, null, null, 1, null, null, null ),
                new NotificacaoInputDTO( List.of( "som" ), null, agora, agora.plusMinutes( 10 ), null ) ) );

        assertThat( criado.notificacao( ).proximaExecucao( ) )
                .isAfter( agora )
                .isBefore( agora.plusMinutes( 10 ) );
    }

    @Test
    @DisplayName("cron por dias da semana agenda quando há horário")
    void cronPorDiasDaSemana_deveAgendar( )
    {
        LembreteOutputDTO criado = lembreteService.adicionar( new LembreteInputDTO(
                "Reunião de alinhamento", "segunda e quarta às 09:00", categoriaId( "#43A047", 104 ),
                new RecorrenciaInputDTO( null, null, null, null, null, null,
                        List.of( EnumDiasDaSemana.SEGUNDA, EnumDiasDaSemana.QUARTA ) ),
                new NotificacaoInputDTO( List.of( "e-mail" ), LocalTime.of( 9, 0 ),
                        LocalDateTime.now( ), null, null ) ) );

        assertThat( criado.notificacao( ).proximaExecucao( ) ).isAfter( LocalDateTime.now( ) );
    }

    @Test
    @DisplayName("datas específicas agendam a menor data futura")
    void datasEspecificas_deveAgendarAMenorFutura( )
    {
        LocalDateTime agora = LocalDateTime.now( );
        LocalDateTime maisProxima = agora.plusMinutes( 5 );

        LembreteOutputDTO criado = lembreteService.adicionar( new LembreteInputDTO(
                "Consulta médica", "duas datas fixas", categoriaId( "#8E24AA", 105 ), null,
                new NotificacaoInputDTO( List.of( "pop-up" ), null, agora, null,
                        List.of( agora.plusMinutes( 30 ), maisProxima ) ) ) );

        // Janela entre as duas datas: prova que escolheu a mais próxima, e não a de 30 min.
        assertThat( criado.notificacao( ).proximaExecucao( ) )
                .isAfter( agora )
                .isBefore( maisProxima.plusMinutes( 1 ) );
    }
}
