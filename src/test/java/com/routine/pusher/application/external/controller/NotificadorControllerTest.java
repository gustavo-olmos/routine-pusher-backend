package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.usecase.NotificacaoUseCase;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Fixa o contrato do fluxo com o balanceador na frente: os cabeçalhos saem na hora, o aviso chega
 * como evento de dados e o batimento não encosta no {@code onmessage} do front.
 */
@ExtendWith(MockitoExtension.class)
class NotificadorControllerTest
{
    private static final Duration ESPERA = Duration.ofSeconds( 3 );

    @Mock
    private NotificacaoUseCase<Lembrete> useCase;

    @Mock
    private SessaoAtualPort sessaoAtual;

    @InjectMocks
    private NotificadorSSEController controller;

    private final Sinks.Many<String> fonte = Sinks.many( ).unicast( ).onBackpressureBuffer( );

    @BeforeEach
    void ligarFonte( )
    {
        UUID sessao = UUID.randomUUID( );
        when( sessaoAtual.uuid( ) ).thenReturn( sessao );
        when( useCase.obterFluxoNotificacoes( sessao ) ).thenReturn( fonte.asFlux( ) );
    }

    @Test
    @DisplayName("sem nenhum disparo, um batimento sai na hora: sem ele o balanceador devolve 504")
    void batimentoImediato( )
    {
        ServerSentEvent<String> primeiro = controller.subscribe( ).blockFirst( ESPERA );

        assertThat( primeiro ).isNotNull( );
        assertThat( primeiro.comment( ) ).isEqualTo( "ping" );
        assertThat( primeiro.data( ) ).isNull( );
    }

    @Test
    @DisplayName("o aviso chega como evento de dados com o título cru, sem comentário")
    void avisoComoDados( )
    {
        fonte.tryEmitNext( "Beber água" );

        ServerSentEvent<String> aviso = controller.subscribe( )
                .filter( evento -> evento.data( ) != null )
                .blockFirst( ESPERA );

        assertThat( aviso ).isNotNull( );
        assertThat( aviso.data( ) ).isEqualTo( "Beber água" );
        assertThat( aviso.comment( ) ).isNull( );
    }

    @Test
    @DisplayName("quando o fluxo da sessão termina, os batimentos param e a conexão fecha")
    void fimDosAvisosEncerraBatimentos( )
    {
        fonte.tryEmitComplete( );

        // block() com prazo: se o intervalo não estivesse amarrado ao fim, isto estouraria.
        List<ServerSentEvent<String>> eventos = controller.subscribe( ).collectList( ).block( ESPERA );

        assertThat( eventos ).isNotNull( ).allMatch( evento -> evento.data( ) == null );
    }
}
