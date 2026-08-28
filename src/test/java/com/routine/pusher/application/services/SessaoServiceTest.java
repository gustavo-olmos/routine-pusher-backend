package com.routine.pusher.application.services;

import com.routine.pusher.application.job.AgendadorJob;
import com.routine.pusher.application.service.SessaoService;
import com.routine.pusher.application.usecase.NotificacaoUseCase;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import com.routine.pusher.core.domain.lembrete.LembreteRepository;
import com.routine.pusher.core.domain.sessao.SessaoAnonima;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaEntity;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaRepository;
import com.routine.pusher.core.domain.sessao.dto.SessaoOutputDTO;
import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
import com.routine.pusher.infrastructure.exceptions.ProcessoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A faxina é onde a sessão anônima vira política de custo: o que expirou morre com tudo que criou.
 * Os casos fixam a ordem do desmonte (agendamento → lembrete → fluxo → sessão), a exceção de quem
 * ainda está ouvindo o SSE e a resiliência — uma sessão emperrada não segura a coleta das demais.
 */
@ExtendWith(MockitoExtension.class)
class SessaoServiceTest
{
    @InjectMocks
    private SessaoService service;

    @Mock
    private SessaoAnonimaRepository sessaoRepository;

    @Mock
    private LembreteRepository lembreteRepository;

    @Mock
    private SessaoAtualPort sessaoAtual;

    @Mock
    private AgendadorJob agendadorJob;

    @Mock
    private NotificacaoUseCase<Lembrete> notificacaoUseCase;


    private SessaoAnonimaEntity sessao( long id )
    {
        SessaoAnonimaEntity sessao = new SessaoAnonimaEntity( );
        sessao.setId( id );
        sessao.setUuid( UUID.randomUUID( ) );
        sessao.setDataCriacao( LocalDateTime.now( ).minusHours( 2 ) );
        sessao.setUltimoAcesso( LocalDateTime.now( ).minusHours( 1 ) );
        return sessao;
    }

    private LembreteEntity lembreteDe( SessaoAnonimaEntity sessao )
    {
        LembreteEntity lembrete = new LembreteEntity( );
        lembrete.setId( 10L );
        lembrete.setUuid( UUID.randomUUID( ) );
        lembrete.setSessao( sessao );
        return lembrete;
    }

    @Test
    @DisplayName("desmonta na ordem inversa da criação: agendamento, lembretes, fluxo SSE, sessão")
    void remocao_desmontaNaOrdemInversa( )
    {
        SessaoAnonimaEntity expirada = sessao( 1L );
        LembreteEntity lembrete = lembreteDe( expirada );

        when( sessaoRepository.findByUltimoAcessoBefore( any( ) ) ).thenReturn( List.of( expirada ) );
        when( notificacaoUseCase.possuiOuvinte( expirada.getUuid( ) ) ).thenReturn( false );
        when( lembreteRepository.findBySessao_Id( 1L ) ).thenReturn( List.of( lembrete ) );

        int removidas = service.removerExpiradas( );

        assertThat( removidas ).isEqualTo( 1 );

        InOrder ordem = inOrder( agendadorJob, lembreteRepository, notificacaoUseCase, sessaoRepository );
        ordem.verify( agendadorJob ).cancelar( lembrete.getUuid( ) );
        ordem.verify( lembreteRepository ).deleteAll( List.of( lembrete ) );
        ordem.verify( notificacaoUseCase ).encerrarFluxo( expirada.getUuid( ) );
        ordem.verify( sessaoRepository ).delete( expirada );
    }

    @Test
    @DisplayName("aba aberta no SSE conta como presença: a sessão não é varrida no meio da espera")
    void sessaoComOuvinte_naoEhVarrida( )
    {
        SessaoAnonimaEntity assistindo = sessao( 1L );
        SessaoAnonimaEntity abandonada = sessao( 2L );

        when( sessaoRepository.findByUltimoAcessoBefore( any( ) ) )
                .thenReturn( List.of( assistindo, abandonada ) );
        when( notificacaoUseCase.possuiOuvinte( assistindo.getUuid( ) ) ).thenReturn( true );
        when( notificacaoUseCase.possuiOuvinte( abandonada.getUuid( ) ) ).thenReturn( false );
        when( lembreteRepository.findBySessao_Id( 2L ) ).thenReturn( List.of( ) );

        int removidas = service.removerExpiradas( );

        assertThat( removidas ).isEqualTo( 1 );
        verify( sessaoRepository, never( ) ).delete( assistindo );
        verify( sessaoRepository ).delete( abandonada );
    }

    @Test
    @DisplayName("agendamento que não cancela não impede a limpeza do dado")
    void cancelamentoFalhou_aindaLimpa( )
    {
        SessaoAnonimaEntity expirada = sessao( 1L );
        LembreteEntity lembrete = lembreteDe( expirada );

        when( sessaoRepository.findByUltimoAcessoBefore( any( ) ) ).thenReturn( List.of( expirada ) );
        when( lembreteRepository.findBySessao_Id( 1L ) ).thenReturn( List.of( lembrete ) );
        doThrow( new ProcessoException( "Cancelamento de lembrete", "quartz fora do ar" ) )
                .when( agendadorJob ).cancelar( lembrete.getUuid( ) );

        assertThat( service.removerExpiradas( ) ).isEqualTo( 1 );
        verify( lembreteRepository ).deleteAll( List.of( lembrete ) );
        verify( sessaoRepository ).delete( expirada );
    }

    @Test
    @DisplayName("uma sessão emperrada não segura a coleta das demais")
    void sessaoEmperrada_naoParaAsDemais( )
    {
        SessaoAnonimaEntity emperrada = sessao( 1L );
        SessaoAnonimaEntity saudavel = sessao( 2L );

        when( sessaoRepository.findByUltimoAcessoBefore( any( ) ) )
                .thenReturn( List.of( emperrada, saudavel ) );
        when( lembreteRepository.findBySessao_Id( any( ) ) ).thenReturn( List.of( ) );
        doThrow( new RuntimeException( "deadlock" ) ).doNothing( )
                .when( lembreteRepository ).deleteAll( anyList( ) );

        assertThat( service.removerExpiradas( ) ).isEqualTo( 1 );
        verify( sessaoRepository ).delete( saudavel );
        verify( sessaoRepository, never( ) ).delete( emperrada );
    }

    @Test
    @DisplayName("encerramento voluntário remove a sessão da requisição atual")
    void encerrar_removeASessaoAtual( )
    {
        SessaoAnonimaEntity minha = sessao( 1L );
        when( sessaoAtual.uuid( ) ).thenReturn( minha.getUuid( ) );
        when( sessaoRepository.findByUuid( minha.getUuid( ) ) ).thenReturn( Optional.of( minha ) );
        when( lembreteRepository.findBySessao_Id( 1L ) ).thenReturn( List.of( ) );

        service.encerrar( );

        verify( sessaoRepository ).delete( minha );
        verify( notificacaoUseCase ).encerrarFluxo( minha.getUuid( ) );
    }

    @Test
    @DisplayName("encerrar sessão que o banco já não tem é um não-evento, não um erro")
    void encerrarSessaoInexistente_naoFazNada( )
    {
        when( sessaoAtual.uuid( ) ).thenReturn( UUID.randomUUID( ) );
        when( sessaoRepository.findByUuid( any( ) ) ).thenReturn( Optional.empty( ) );

        service.encerrar( );

        verify( sessaoRepository, never( ) ).delete( any( SessaoAnonimaEntity.class ) );
    }

    @Test
    @DisplayName("a validade informada ao front anda com o uso: último acesso + janela")
    void atual_calculaValidadePeloUltimoAcesso( )
    {
        SessaoAnonimaEntity minha = sessao( 1L );
        when( sessaoAtual.uuid( ) ).thenReturn( minha.getUuid( ) );
        when( sessaoRepository.findByUuid( minha.getUuid( ) ) ).thenReturn( Optional.of( minha ) );

        SessaoOutputDTO dto = service.atual( );

        assertThat( dto.uuid( ) ).isEqualTo( minha.getUuid( ) );
        assertThat( dto.expiraEm( ) )
                .isEqualTo( minha.getUltimoAcesso( ).plus( SessaoAnonima.JANELA_INATIVIDADE ) );
    }
}
