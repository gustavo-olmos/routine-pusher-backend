package com.routine.pusher.application.services;

import com.routine.pusher.application.external.client.ChatClient;
import com.routine.pusher.application.service.LembreteChatService;
import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.application.usecase.SimulacaoUseCase;
import com.routine.pusher.core.domain.categoria.port.CategoriaQueryPort;
import com.routine.pusher.core.domain.lembrete.dto.InterpretacaoLembreteOutputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteChatInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.sessao.SessaoAnonima;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaRepository;
import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
import com.routine.pusher.example.LembreteExample;
import com.routine.pusher.infrastructure.exceptions.ConversaoException;
import com.routine.pusher.infrastructure.exceptions.LimiteDeUsoException;
import com.routine.pusher.infrastructure.exceptions.StrategyException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * As regras que não dependem de modelo nenhum: a cota por sessão consome antes de gastar, o relógio
 * é o de quem pede, e o que o modelo devolve passa pela mesma validação da porta humana.
 */
@ExtendWith(MockitoExtension.class)
class LembreteChatServiceTest
{
    private static final UUID SESSAO = UUID.randomUUID( );

    @Mock
    private ChatClient<LembreteInputDTO> client;

    @Mock
    private CRUDUseCase<LembreteInputDTO, LembreteOutputDTO, UUID> crudUseCase;

    @Mock
    private SimulacaoUseCase<LembreteInputDTO, List<LocalDateTime>> simulacao;

    @Mock
    private CategoriaQueryPort categoriaQueryPort;

    @Mock
    private SessaoAtualPort sessaoAtual;

    @Mock
    private SessaoAnonimaRepository sessaoRepository;

    /** Validator real: o teste deve reprovar pelo mesmo motivo que a produção reprovaria. */
    private final Validator validator = Validation.buildDefaultValidatorFactory( ).getValidator( );

    private LembreteChatService service;

    @BeforeEach
    void montarServico( )
    {
        service = new LembreteChatService( client, crudUseCase, simulacao, categoriaQueryPort,
                sessaoAtual, sessaoRepository, validator );
        // Lenient: a recusa por falta de vaga acontece antes de qualquer consulta à sessão.
        lenient( ).when( sessaoAtual.uuid( ) ).thenReturn( SESSAO );
    }

    private void comCota( )
    {
        when( sessaoRepository.consumirChamadaIa( SESSAO, SessaoAnonima.LIMITE_CHAMADAS_IA ) )
                .thenReturn( 1 );
    }

    @Test
    @DisplayName("frase válida vira lembrete: modelo monta, validação aprova, criação segue o fluxo normal")
    void fraseValida_viraLembrete( )
    {
        comCota( );
        LembreteInputDTO montado = LembreteExample.simplesInput( );
        when( client.montarLembrete( anyString( ), any( LocalDateTime.class ), anyList( ) ) )
                .thenReturn( montado );

        service.criarLembreteViaChat( new LembreteChatInputDTO( "beber água a cada 2h", null ) );

        verify( crudUseCase ).adicionar( montado );
    }

    @Test
    @DisplayName("sem cota, nada é gasto: o modelo nem chega a ser chamado")
    void semCota_naoChamaOModelo( )
    {
        when( sessaoRepository.consumirChamadaIa( SESSAO, SessaoAnonima.LIMITE_CHAMADAS_IA ) )
                .thenReturn( 0 );

        assertThatThrownBy( ( ) -> service.criarLembreteViaChat(
                new LembreteChatInputDTO( "qualquer frase", null ) ) )
                .isInstanceOf( LimiteDeUsoException.class );

        verify( client, never( ) ).montarLembrete( anyString( ), any( ), anyList( ) );
        verify( crudUseCase, never( ) ).adicionar( any( ) );
    }

    @Test
    @DisplayName("lembrete inválido do modelo é barrado pela mesma régua do controller, sem persistir nada")
    void lembreteInvalidoDoModelo_ehBarrado( )
    {
        comCota( );
        // Sem título e sem notificação: violações que o Bean Validation do DTO precisa apanhar.
        when( client.montarLembrete( anyString( ), any( LocalDateTime.class ), anyList( ) ) )
                .thenReturn( new LembreteInputDTO( "", null, 1L, null, null ) );

        assertThatThrownBy( ( ) -> service.criarLembreteViaChat(
                new LembreteChatInputDTO( "frase confusa", null ) ) )
                .isInstanceOf( ConversaoException.class )
                .hasMessageContaining( "reformular" );

        verify( crudUseCase, never( ) ).adicionar( any( ) );
    }

    @Test
    @DisplayName("o relógio é o de quem pede: o 'agora' do front chega intacto ao modelo")
    void relogioDoUsuario_chegaAoModelo( )
    {
        comCota( );
        LocalDateTime relogioDoUsuario = LocalDateTime.of( 2026, 8, 28, 22, 45 );
        when( client.montarLembrete( anyString( ), any( LocalDateTime.class ), anyList( ) ) )
                .thenReturn( LembreteExample.simplesInput( ) );

        service.criarLembreteViaChat( new LembreteChatInputDTO( "amanhã às 9h", relogioDoUsuario ) );

        verify( client ).montarLembrete( eq( "amanhã às 9h" ), eq( relogioDoUsuario ), anyList( ) );
    }

    @Test
    @DisplayName("sem relógio do front, vale o do servidor — nunca um nulo dentro do prompt")
    void semRelogioDoFront_valeODoServidor( )
    {
        comCota( );
        when( client.montarLembrete( anyString( ), any( LocalDateTime.class ), anyList( ) ) )
                .thenReturn( LembreteExample.simplesInput( ) );

        service.criarLembreteViaChat( new LembreteChatInputDTO( "daqui a 10 minutos", null ) );

        ArgumentCaptor<LocalDateTime> agora = ArgumentCaptor.forClass( LocalDateTime.class );
        verify( client ).montarLembrete( anyString( ), agora.capture( ), anyList( ) );
        assertThat( agora.getValue( ) ).isCloseTo( LocalDateTime.now( ),
                org.assertj.core.api.Assertions.within( 5, java.time.temporal.ChronoUnit.SECONDS ) );
    }

    // --- interpretação: interpretar -> revisar -> criar ---------------------------------------

    @Test
    @DisplayName("interpretar devolve o rascunho e a prévia, e não cria nada")
    void interpretar_devolveRascunhoSemCriar( )
    {
        comCota( );
        LembreteInputDTO montado = LembreteExample.simplesInput( );
        List<LocalDateTime> previa = List.of( LocalDateTime.of( 2026, 12, 1, 9, 0 ),
                                              LocalDateTime.of( 2026, 12, 3, 9, 0 ) );
        when( client.montarLembrete( anyString( ), any( LocalDateTime.class ), anyList( ) ) )
                .thenReturn( montado );
        when( simulacao.simular( montado ) ).thenReturn( previa );

        InterpretacaoLembreteOutputDTO resposta = service.interpretarLembrete(
                new LembreteChatInputDTO( "beber água a cada 2h", null ) );

        assertThat( resposta.lembrete( ) ).isSameAs( montado );
        assertThat( resposta.proximasExecucoes( ) ).isEqualTo( previa );
        verify( crudUseCase, never( ) ).adicionar( any( ) );
    }

    @Test
    @DisplayName("interpretar consome uma chamada da cota de IA, depois de conferir a vaga")
    void interpretar_consomeCotaDepoisDaVaga( )
    {
        comCota( );
        when( client.montarLembrete( anyString( ), any( LocalDateTime.class ), anyList( ) ) )
                .thenReturn( LembreteExample.simplesInput( ) );

        service.interpretarLembrete( new LembreteChatInputDTO( "frase", null ) );

        var ordem = inOrder( simulacao, sessaoRepository, client );
        ordem.verify( simulacao ).validarVagaNaSessao( );
        ordem.verify( sessaoRepository ).consumirChamadaIa( SESSAO, SessaoAnonima.LIMITE_CHAMADAS_IA );
        ordem.verify( client ).montarLembrete( anyString( ), any( ), anyList( ) );
    }

    @Test
    @DisplayName("interpretar sem cota de IA recusa sem chamar o modelo")
    void interpretar_semCota_naoChamaOModelo( )
    {
        when( sessaoRepository.consumirChamadaIa( SESSAO, SessaoAnonima.LIMITE_CHAMADAS_IA ) )
                .thenReturn( 0 );

        assertThatThrownBy( ( ) -> service.interpretarLembrete(
                new LembreteChatInputDTO( "qualquer frase", null ) ) )
                .isInstanceOf( LimiteDeUsoException.class );

        verify( client, never( ) ).montarLembrete( anyString( ), any( ), anyList( ) );
        verify( simulacao, never( ) ).simular( any( ) );
    }

    @Test
    @DisplayName("sessão no limite de lembretes falha antes de gastar cota de IA")
    void interpretar_semVaga_naoGastaCota( )
    {
        doThrow( new LimiteDeUsoException( "limite de lembretes" ) )
                .when( simulacao ).validarVagaNaSessao( );

        assertThatThrownBy( ( ) -> service.interpretarLembrete(
                new LembreteChatInputDTO( "qualquer frase", null ) ) )
                .isInstanceOf( LimiteDeUsoException.class );

        verify( sessaoRepository, never( ) ).consumirChamadaIa( any( ), anyInt( ) );
        verify( client, never( ) ).montarLembrete( anyString( ), any( ), anyList( ) );
    }

    @Test
    @DisplayName("rascunho inválido do modelo falha com o campo na mensagem, sem chegar à simulação")
    void interpretar_rascunhoInvalido_apontaOCampo( )
    {
        comCota( );
        when( client.montarLembrete( anyString( ), any( LocalDateTime.class ), anyList( ) ) )
                .thenReturn( new LembreteInputDTO( "", null, 1L, null, null ) );

        assertThatThrownBy( ( ) -> service.interpretarLembrete(
                new LembreteChatInputDTO( "frase confusa", null ) ) )
                .isInstanceOf( ConversaoException.class )
                .hasMessageContaining( "titulo" )
                .hasMessageContaining( "notificacao" );

        verify( simulacao, never( ) ).simular( any( ) );
    }

    @Test
    @DisplayName("recusa de domínio na simulação chega ao cliente como está")
    void interpretar_recusaDoDominio_propaga( )
    {
        comCota( );
        LembreteInputDTO montado = LembreteExample.simplesInput( );
        when( client.montarLembrete( anyString( ), any( LocalDateTime.class ), anyList( ) ) )
                .thenReturn( montado );
        when( simulacao.simular( montado ) )
                .thenThrow( new StrategyException( "Intervalo de repetição muito curto" ) );

        assertThatThrownBy( ( ) -> service.interpretarLembrete(
                new LembreteChatInputDTO( "de minuto em minuto", null ) ) )
                .isInstanceOf( StrategyException.class );
    }
}
