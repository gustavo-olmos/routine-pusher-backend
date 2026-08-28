package com.routine.pusher.application.services;

import com.routine.pusher.application.job.AgendadorJob;
import com.routine.pusher.application.service.LembreteService;
import com.routine.pusher.core.domain.categoria.port.CategoriaQueryPort;
import com.routine.pusher.core.domain.feriado.port.FeriadoPort;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import com.routine.pusher.core.domain.lembrete.LembreteMapperImpl;
import com.routine.pusher.core.domain.lembrete.LembreteRepository;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.notificacao.Notificacao;
import com.routine.pusher.core.domain.sessao.SessaoAnonima;
import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
import com.routine.pusher.example.LembreteExample;
import com.routine.pusher.infrastructure.exceptions.LimiteDeUsoException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Com a sessão anônima, todo acesso a lembrete passou a ser escopado: o visitante só enxerga o que
 * criou. Estes casos fixam o contrato — consulta escopada em cada operação, 404 indistinguível para
 * o lembrete alheio, e a trava de custo na criação.
 */
@ExtendWith(MockitoExtension.class)
class LembreteEscopoSessaoTest
{
    private static final UUID SESSAO = UUID.randomUUID( );

    @InjectMocks
    private LembreteService service;

    @Mock
    private LembreteMapperImpl mapper;

    @Mock
    private LembreteRepository repository;

    @Mock
    private CategoriaQueryPort categoriaQueryPort;

    @Mock
    private AgendadorJob agendadorJob;

    @Mock
    private FeriadoPort feriadoPort;

    @Mock
    private SessaoAtualPort sessaoAtual;


    @Test
    @DisplayName("criar carimba a sessão da requisição no lembrete antes de persistir")
    void adicionar_carimbaASessaoAtual( )
    {
        LembreteInputDTO input = LembreteExample.simplesInput( );
        Lembrete lembrete = new Lembrete( );
        Notificacao notificacao = mock( Notificacao.class );
        lembrete.setNotificacao( notificacao );

        when( sessaoAtual.uuid( ) ).thenReturn( SESSAO );
        when( repository.countBySessao_Uuid( SESSAO ) ).thenReturn( 0L );
        when( mapper.toDomain( input ) ).thenReturn( lembrete );
        when( notificacao.aindaTemNotificacao( lembrete, feriadoPort ) ).thenReturn( false );
        when( mapper.toEntity( lembrete ) ).thenReturn( new LembreteEntity( ) );
        when( repository.save( any( LembreteEntity.class ) ) ).thenAnswer( inv -> inv.getArgument( 0 ) );
        when( mapper.toDomain( any( LembreteEntity.class ) ) ).thenReturn( lembrete );

        service.adicionar( input );

        assertThat( lembrete.getSessaoUuid( ) ).isEqualTo( SESSAO );
    }

    @Test
    @DisplayName("no teto de lembretes a criação é recusada antes de tocar qualquer outra coisa")
    void adicionar_noTeto_recusa( )
    {
        when( sessaoAtual.uuid( ) ).thenReturn( SESSAO );
        when( repository.countBySessao_Uuid( SESSAO ) )
                .thenReturn( (long) SessaoAnonima.LIMITE_LEMBRETES );

        assertThatThrownBy( ( ) -> service.adicionar( LembreteExample.simplesInput( ) ) )
                .isInstanceOf( LimiteDeUsoException.class );

        verify( repository, never( ) ).save( any( ) );
        verify( agendadorJob, never( ) ).agendar( any( ) );
    }

    @Test
    @DisplayName("listar devolve apenas os lembretes da sessão da requisição")
    void listar_soDaSessao( )
    {
        when( sessaoAtual.uuid( ) ).thenReturn( SESSAO );
        when( repository.findBySessao_Uuid( SESSAO ) ).thenReturn( List.of( ) );

        assertThat( service.listar( "titulo", false ) ).isEmpty( );

        verify( repository, never( ) ).findAll( );
    }

    @Test
    @DisplayName("uuid de outra sessão responde 404 — sem revelar que o lembrete existe")
    void atualizar_lembreteAlheio_naoEncontrado( )
    {
        UUID alheio = UUID.randomUUID( );
        when( sessaoAtual.uuid( ) ).thenReturn( SESSAO );
        when( repository.findByUuidAndSessao_Uuid( alheio, SESSAO ) ).thenReturn( Optional.empty( ) );

        assertThatThrownBy( ( ) -> service.atualizar( alheio, LembreteExample.simplesInput( ) ) )
                .isInstanceOf( EntityNotFoundException.class );

        // O caminho sem escopo continua existindo para uso interno (executor de jobs), mas o
        // endpoint jamais passa por ele.
        verify( repository, never( ) ).findByUuid( any( ) );
    }

    @Test
    @DisplayName("excluir também só encontra dentro do escopo da sessão")
    void excluir_lembreteAlheio_naoEncontrado( )
    {
        UUID alheio = UUID.randomUUID( );
        when( sessaoAtual.uuid( ) ).thenReturn( SESSAO );
        when( repository.findByUuidAndSessao_Uuid( alheio, SESSAO ) ).thenReturn( Optional.empty( ) );

        assertThatThrownBy( ( ) -> service.excluir( alheio ) )
                .isInstanceOf( EntityNotFoundException.class );

        verify( repository, never( ) ).delete( any( LembreteEntity.class ) );
    }
}
