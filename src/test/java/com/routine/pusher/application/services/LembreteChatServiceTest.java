package com.routine.pusher.application.services;

import com.routine.pusher.application.external.client.ChatClient;
import com.routine.pusher.application.service.LembreteChatService;
import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.core.domain.categoria.port.CategoriaQueryPort;
import com.routine.pusher.core.domain.lembrete.dto.LembreteChatInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.sessao.SessaoAnonima;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaRepository;
import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
import com.routine.pusher.example.LembreteExample;
import com.routine.pusher.infrastructure.exceptions.ConversaoException;
import com.routine.pusher.infrastructure.exceptions.LimiteDeUsoException;
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
        service = new LembreteChatService( client, crudUseCase, categoriaQueryPort,
                sessaoAtual, sessaoRepository, validator );
        when( sessaoAtual.uuid( ) ).thenReturn( SESSAO );
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
}
