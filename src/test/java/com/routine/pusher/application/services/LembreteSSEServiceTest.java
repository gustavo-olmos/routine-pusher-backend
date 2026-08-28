package com.routine.pusher.application.services;

import com.routine.pusher.application.service.LembreteSSEService;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import com.routine.pusher.core.domain.lembrete.LembreteMapperImpl;
import com.routine.pusher.core.domain.lembrete.LembreteRepository;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.Disposable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

/**
 * O SSE deixou de ser um alto-falante da aplicação inteira: cada sessão tem o próprio fluxo. Estes
 * casos fixam o isolamento (a notificação chega só no dono), a persistência do disparo mesmo sem
 * ouvinte e o ciclo de vida do fluxo quando a sessão morre.
 */
@ExtendWith(MockitoExtension.class)
class LembreteSSEServiceTest
{
    @InjectMocks
    private LembreteSSEService service;

    @Mock
    private LembreteMapperImpl mapper;

    @Mock
    private LembreteRepository repository;


    private Lembrete lembreteDa( UUID sessao )
    {
        Lembrete lembrete = new Lembrete( );
        lembrete.setTitulo( "Beber água" );
        lembrete.setSessaoUuid( sessao );
        lembrete.setNotificacao( new Notificacao( ) );
        lenient( ).when( mapper.toEntity( any( Lembrete.class ) ) ).thenReturn( new LembreteEntity( ) );
        return lembrete;
    }

    @Test
    @DisplayName("a notificação chega só na sessão dona do lembrete")
    void notificacaoNaoVazaEntreSessoes( )
    {
        UUID dona = UUID.randomUUID( );
        UUID vizinha = UUID.randomUUID( );

        List<String> recebidasPelaDona = new ArrayList<>( );
        List<String> recebidasPelaVizinha = new ArrayList<>( );
        service.obterFluxoNotificacoes( dona ).subscribe( recebidasPelaDona::add );
        service.obterFluxoNotificacoes( vizinha ).subscribe( recebidasPelaVizinha::add );

        service.adicionarEnvio( lembreteDa( dona ) );

        assertThat( recebidasPelaDona ).containsExactly( "Beber água" );
        assertThat( recebidasPelaVizinha ).isEmpty( );
    }

    @Test
    @DisplayName("sem ouvinte o disparo persiste do mesmo jeito: cota e reagendamento não podem divergir")
    void semOuvinte_persisteMesmoAssim( )
    {
        Lembrete lembrete = lembreteDa( UUID.randomUUID( ) );

        service.adicionarEnvio( lembrete );

        assertThat( lembrete.getNotificacao( ).getUltimaExecucao( ) ).isNotNull( );
        verify( repository ).save( any( LembreteEntity.class ) );
    }

    @Test
    @DisplayName("presença é ter inscrito agora: vale enquanto a conexão vive, e só enquanto vive")
    void possuiOuvinte_acompanhaAConexao( )
    {
        UUID sessao = UUID.randomUUID( );
        assertThat( service.possuiOuvinte( sessao ) ).isFalse( );

        Disposable conexao = service.obterFluxoNotificacoes( sessao ).subscribe( );
        assertThat( service.possuiOuvinte( sessao ) ).isTrue( );

        conexao.dispose( );
        assertThat( service.possuiOuvinte( sessao ) ).isFalse( );
    }

    @Test
    @DisplayName("sessão removida encerra o fluxo: o navegador recebe o fim, não um limbo")
    void encerrarFluxo_completaParaOAssinante( )
    {
        UUID sessao = UUID.randomUUID( );
        AtomicBoolean encerrado = new AtomicBoolean( false );
        service.obterFluxoNotificacoes( sessao )
                .subscribe( ignorado -> { }, erro -> { }, ( ) -> encerrado.set( true ) );

        service.encerrarFluxo( sessao );

        assertThat( encerrado ).isTrue( );
        assertThat( service.possuiOuvinte( sessao ) ).isFalse( );
    }

    @Test
    @DisplayName("duas abas da mesma sessão recebem a mesma notificação")
    void duasAbas_mesmaSessao_ambasRecebem( )
    {
        UUID sessao = UUID.randomUUID( );
        List<String> abaUm = new ArrayList<>( );
        List<String> abaDois = new ArrayList<>( );
        service.obterFluxoNotificacoes( sessao ).subscribe( abaUm::add );
        service.obterFluxoNotificacoes( sessao ).subscribe( abaDois::add );

        service.adicionarEnvio( lembreteDa( sessao ) );

        assertThat( abaUm ).containsExactly( "Beber água" );
        assertThat( abaDois ).containsExactly( "Beber água" );
    }
}
