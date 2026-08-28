package com.routine.pusher.application.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.routine.pusher.application.service.LembreteService;
import com.routine.pusher.core.domain.categoria.CategoriaEntity;
import com.routine.pusher.core.domain.categoria.CategoriaRepository;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.notificacao.dto.NotificacaoInputDTO;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaInputDTO;
import com.routine.pusher.core.domain.feriado.port.FeriadoPort;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaEntity;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaRepository;
import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import com.routine.pusher.core.enums.EnumPoliticaDiaUtil;
import com.routine.pusher.infrastructure.exceptions.StrategyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

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
    private SessaoAnonimaRepository sessaoRepository;
    @Autowired
    private LembreteService lembreteService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FeriadoPort feriadoPort;

    /** Fora de uma requisição HTTP não há cookie: a porta é mockada, mas a sessão é real no banco. */
    @MockBean
    private SessaoAtualPort sessaoAtual;

    @BeforeEach
    void abrirSessaoAnonima( )
    {
        SessaoAnonimaEntity sessao = new SessaoAnonimaEntity( );
        sessao.setUuid( UUID.randomUUID( ) );
        sessao.setDataCriacao( LocalDateTime.now( ) );
        sessao.setUltimoAcesso( LocalDateTime.now( ) );

        when( sessaoAtual.uuid( ) ).thenReturn( sessaoRepository.save( sessao ).getUuid( ) );
    }

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
                new RecorrenciaInputDTO( null, null, null, 2, null, null, null , null ),
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
                new RecorrenciaInputDTO( 2, null, null, 3, null, null, null , null ),
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
                new RecorrenciaInputDTO( null, null, null, 1, null, null, null , null ),
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
                        List.of( EnumDiasDaSemana.SEGUNDA, EnumDiasDaSemana.QUARTA ) , null ),
                new NotificacaoInputDTO( List.of( "e-mail" ), LocalTime.of( 9, 0 ),
                        LocalDateTime.now( ), null, null ) ) );

        assertThat( criado.notificacao( ).proximaExecucao( ) ).isAfter( LocalDateTime.now( ) );
    }

    @Test
    @DisplayName("a saída antecipa as próximas 5 execuções e as serializa como lista ISO")
    void proximasExecucoes_saemNaRespostaEmFormatoISO( ) throws JsonProcessingException
    {
        LocalDateTime agora = LocalDateTime.now( );

        LembreteOutputDTO criado = lembreteService.adicionar( new LembreteInputDTO(
                "Pomodoro", "de 25 em 25 minutos", categoriaId( "#00897B", 106 ),
                new RecorrenciaInputDTO( null, null, null, 25, null, null, null , null ),
                new NotificacaoInputDTO( List.of( "pop-up" ), null, agora, null, null ) ) );

        assertThat( criado.proximasExecucoes( ) )
                .hasSize( 5 )
                .isSorted( )
                .allMatch( data -> data.isAfter( agora ) );

        // O consumidor da API lê a lista já formatada; sem serializer, Jackson quebraria o
        // LocalDateTime em array de inteiros e o front teria de remontar a data.
        String primeira = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format( criado.proximasExecucoes( ).get( 0 ) );

        assertThat( objectMapper.writeValueAsString( criado ) )
                .contains( "\"proximasExecucoes\":[\"" + primeira + "\"" );
    }

    @Test
    @DisplayName("cota restante limita a prévia: 2 disparos, 2 execuções previstas")
    void proximasExecucoes_respeitamACota( )
    {
        LembreteOutputDTO criado = lembreteService.adicionar( new LembreteInputDTO(
                "Tomar remédio", "de hora em hora, 2 vezes", categoriaId( "#6D4C41", 107 ),
                new RecorrenciaInputDTO( 2, null, 1, null, null, null, null , null ),
                new NotificacaoInputDTO( List.of( "som" ), null, LocalDateTime.now( ), null, null ) ) );

        assertThat( criado.proximasExecucoes( ) ).hasSize( 2 );
    }

    @Test
    @DisplayName("dias úteis: os cinco dias da semana cadastram e agendam de fato")
    void cronPorDiasUteis_deveAgendar( )
    {
        LembreteOutputDTO criado = lembreteService.adicionar( new LembreteInputDTO(
                "Daily", "de segunda a sexta às 09:00", categoriaId( "#3949AB", 108 ),
                new RecorrenciaInputDTO( null, null, null, null, null, null,
                        List.of( EnumDiasDaSemana.SEGUNDA, EnumDiasDaSemana.TERCA,
                                 EnumDiasDaSemana.QUARTA, EnumDiasDaSemana.QUINTA,
                                 EnumDiasDaSemana.SEXTA ) , null ),
                new NotificacaoInputDTO( List.of( "e-mail" ), LocalTime.of( 9, 0 ),
                        LocalDateTime.now( ), null, null ) ) );

        assertThat( criado.recorrencia( ).diasDaSemana( ) ).hasSize( 5 );
        assertThat( criado.notificacao( ).proximaExecucao( ) ).isAfter( LocalDateTime.now( ) );
        assertThat( criado.proximasExecucoes( ) )
                .hasSize( 5 )
                .allMatch( data -> data.toLocalTime( ).equals( LocalTime.of( 9, 0 ) ) )
                .noneMatch( data -> data.getDayOfWeek( ) == DayOfWeek.SATURDAY
                                 || data.getDayOfWeek( ) == DayOfWeek.SUNDAY );
    }

    @Test
    @DisplayName("dias úteis com PULAR agenda e nunca projeta feriado nacional")
    void diasUteisComPolitica_deveAgendarSemFeriado( )
    {
        LembreteOutputDTO criado = lembreteService.adicionar( new LembreteInputDTO(
                "Daily sem feriado", "seg a sex às 09:00, pulando feriado", categoriaId( "#00695C", 109 ),
                new RecorrenciaInputDTO( null, null, null, null, null, null,
                        List.of( EnumDiasDaSemana.SEGUNDA, EnumDiasDaSemana.TERCA,
                                 EnumDiasDaSemana.QUARTA, EnumDiasDaSemana.QUINTA,
                                 EnumDiasDaSemana.SEXTA ), EnumPoliticaDiaUtil.PULAR ),
                new NotificacaoInputDTO( List.of( "e-mail" ), LocalTime.of( 9, 0 ),
                        LocalDateTime.now( ), null, null ) ) );

        assertThat( criado.recorrencia( ).politicaDiaUtil( ) ).isEqualTo( EnumPoliticaDiaUtil.PULAR );
        assertThat( criado.proximasExecucoes( ) )
                .hasSize( 5 )
                .noneMatch( data -> feriadoPort.ehFeriado( data.toLocalDate( ) ) );

        // O disparo que foi realmente agendado precisa ser o mesmo que a prévia anuncia.
        assertThat( criado.notificacao( ).proximaExecucao( ) )
                .isEqualTo( criado.proximasExecucoes( ).get( 0 ) );
    }

    @Test
    @DisplayName("intervalo diário aceita política de dia útil")
    void intervaloDiarioComPolitica_deveAgendar( )
    {
        LembreteOutputDTO criado = lembreteService.adicionar( new LembreteInputDTO(
                "Relatório", "a cada 1 dia, só em dia útil", categoriaId( "#5D4037", 110 ),
                new RecorrenciaInputDTO( null, 1, null, null, null, null, null,
                        EnumPoliticaDiaUtil.PULAR ),
                new NotificacaoInputDTO( List.of( "pop-up" ), null, LocalDateTime.now( ), null, null ) ) );

        assertThat( criado.proximasExecucoes( ) )
                .isNotEmpty( )
                .noneMatch( data -> feriadoPort.ehFeriado( data.toLocalDate( ) ) );
    }

    @Test
    @DisplayName("intervalo abaixo de um dia com política é recusado na porta, não mitigado depois")
    void intervaloCurtoComPolitica_deveRecusar( )
    {
        LembreteInputDTO abusivo = new LembreteInputDTO(
                "Engraçadinho", "1 em 1 minuto pulando feriado", categoriaId( "#C62828", 111 ),
                new RecorrenciaInputDTO( null, null, null, 1, null, null, null,
                        EnumPoliticaDiaUtil.PULAR ),
                new NotificacaoInputDTO( List.of( "pop-up" ), null, LocalDateTime.now( ), null, null ) );

        assertThatThrownBy( () -> lembreteService.adicionar( abusivo ) )
                .isInstanceOf( StrategyException.class )
                .hasMessageContaining( "ao menos um dia" );
    }

    @Test
    @DisplayName("política de dia útil não sobrescreve datas escolhidas a dedo")
    void datasEspecificasComPolitica_deveRecusar( )
    {
        LembreteInputDTO conflitante = new LembreteInputDTO(
                "Consulta", "datas fixas com política", categoriaId( "#AD1457", 112 ),
                new RecorrenciaInputDTO( null, 1, null, null, null, null, null,
                        EnumPoliticaDiaUtil.ADIAR ),
                new NotificacaoInputDTO( List.of( "pop-up" ), null, LocalDateTime.now( ), null,
                        List.of( LocalDateTime.now( ).plusDays( 3 ) ) ) );

        assertThatThrownBy( () -> lembreteService.adicionar( conflitante ) )
                .isInstanceOf( StrategyException.class )
                .hasMessageContaining( "datas específicas" );
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
