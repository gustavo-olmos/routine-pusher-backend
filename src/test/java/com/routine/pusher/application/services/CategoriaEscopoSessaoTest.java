package com.routine.pusher.application.services;

import com.routine.pusher.application.service.CategoriaService;
import com.routine.pusher.core.domain.categoria.CategoriaEntity;
import com.routine.pusher.core.domain.categoria.CategoriaMapperImpl;
import com.routine.pusher.core.domain.categoria.CategoriaRepository;
import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.lembrete.LembreteQueryPort;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaEntity;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaRepository;
import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Desde a V6 a categoria tem dono, e o acesso é escopado como o do lembrete: o visitante só enxerga
 * a própria lista.
 *
 * <p>O caso perigoso não é a listagem — é o id. Ele é sequencial e adivinhável, então sem escopo
 * bastaria pedir {@code /categoria/1} para ler, renomear ou apagar a categoria de outra pessoa, e
 * anexá-la a um lembrete próprio. Estes casos fixam que toda operação por id passa pela sessão.</p>
 */
@ExtendWith(MockitoExtension.class)
class CategoriaEscopoSessaoTest
{
    private static final UUID SESSAO = UUID.randomUUID( );
    private static final Long ID_ALHEIO = 1L;

    @InjectMocks
    private CategoriaService service;

    @Mock
    private CategoriaMapperImpl mapper;

    @Mock
    private CategoriaRepository repository;

    @Mock
    private LembreteQueryPort lembreteQueryPort;

    @Mock
    private SessaoAtualPort sessaoAtual;

    @Mock
    private SessaoAnonimaRepository sessaoRepository;


    private CategoriaInputDTO entrada( )
    {
        return new CategoriaInputDTO( "Financeiro", "#00897B", 3 );
    }

    /** A categoria de outro visitante simplesmente não é encontrada nesta sessão. */
    private void naoExisteNaSessao( )
    {
        when( sessaoAtual.uuid( ) ).thenReturn( SESSAO );
        when( repository.findByIdAndSessao_Uuid( ID_ALHEIO, SESSAO ) ).thenReturn( Optional.empty( ) );
    }


    @Test
    @DisplayName("listar consulta apenas a lista da sessão, nunca findAll")
    void listar_escopaPelaSessao( )
    {
        when( sessaoAtual.uuid( ) ).thenReturn( SESSAO );
        when( repository.findBySessao_Uuid( SESSAO ) ).thenReturn( List.of( ) );

        assertThat( service.listar( "nome", false ) ).isEmpty( );

        verify( repository ).findBySessao_Uuid( SESSAO );
        verify( repository, never( ) ).findAll( );
    }

    @Test
    @DisplayName("adicionar carimba a sessão da requisição na categoria criada")
    void adicionar_carimbaSessao( )
    {
        SessaoAnonimaEntity sessao = new SessaoAnonimaEntity( );
        sessao.setUuid( SESSAO );

        CategoriaEntity nova = new CategoriaEntity( );

        when( sessaoAtual.uuid( ) ).thenReturn( SESSAO );
        when( sessaoRepository.findByUuid( SESSAO ) ).thenReturn( Optional.of( sessao ) );
        when( mapper.toEntity( any( CategoriaInputDTO.class ) ) ).thenReturn( nova );
        when( repository.save( nova ) ).thenReturn( nova );

        service.adicionar( entrada( ) );

        assertThat( nova.getSessao( ) ).isSameAs( sessao );
    }

    @Test
    @DisplayName("buscar categoria de outra sessão responde como inexistente")
    void buscar_deOutraSessao_naoEncontra( )
    {
        naoExisteNaSessao( );

        assertThatThrownBy( () -> service.buscarPeloId( ID_ALHEIO ) )
                .isInstanceOf( EntityNotFoundException.class );
    }

    @Test
    @DisplayName("renomear categoria de outra sessão responde como inexistente")
    void atualizar_deOutraSessao_naoEncontra( )
    {
        naoExisteNaSessao( );

        assertThatThrownBy( () -> service.atualizar( ID_ALHEIO, entrada( ) ) )
                .isInstanceOf( EntityNotFoundException.class );

        verify( repository, never( ) ).save( any( ) );
    }

    @Test
    @DisplayName("excluir categoria de outra sessão responde como inexistente, sem sequer checar lembretes")
    void excluir_deOutraSessao_naoEncontra( )
    {
        naoExisteNaSessao( );

        assertThatThrownBy( () -> service.excluir( ID_ALHEIO ) )
                .isInstanceOf( EntityNotFoundException.class );

        verify( lembreteQueryPort, never( ) ).existeLembreteComCategoriaId( any( ) );
        verify( repository, never( ) ).delete( any( ) );
    }
}
