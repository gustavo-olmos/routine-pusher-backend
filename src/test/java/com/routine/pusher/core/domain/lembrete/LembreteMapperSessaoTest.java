package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.core.domain.sessao.SessaoAnonimaEntity;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaRepository;
import com.routine.pusher.example.LembreteExample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A resolução sessão-UUID → FK mora no mapeamento para que nenhum dos seis pontos que salvam
 * entidade reconstruída possa esquecê-la. Estes casos fixam os três lados do contrato: resolver ao
 * ir para a entidade, extrair ao vir dela, e não tocar na sessão de uma entidade em atualização.
 */
class LembreteMapperSessaoTest
{
    private final LembreteMapperImpl mapper = new LembreteMapperImpl( );

    private SessaoAnonimaEntity sessao( UUID uuid )
    {
        SessaoAnonimaEntity sessao = new SessaoAnonimaEntity( );
        sessao.setId( 7L );
        sessao.setUuid( uuid );
        return sessao;
    }

    @Test
    @DisplayName("toEntity resolve o uuid de sessão do domínio na entidade dona da FK")
    void toEntity_resolveSessaoPeloUuid( )
    {
        UUID uuid = UUID.randomUUID( );
        SessaoAnonimaEntity dona = sessao( uuid );

        mapper.sessaoRepository = mock( SessaoAnonimaRepository.class );
        when( mapper.sessaoRepository.findByUuid( uuid ) ).thenReturn( Optional.of( dona ) );

        Lembrete lembrete = new Lembrete( );
        lembrete.setSessaoUuid( uuid );

        assertThat( mapper.toEntity( lembrete ).getSessao( ) ).isSameAs( dona );
    }

    @Test
    @DisplayName("toDomain extrai o uuid da sessão — é ele que roteia a notificação SSE")
    void toDomain_extraiUuidDaSessao( )
    {
        UUID uuid = UUID.randomUUID( );
        LembreteEntity entity = LembreteExample.entity( );
        entity.setSessao( sessao( uuid ) );

        assertThat( mapper.toDomain( entity ).getSessaoUuid( ) ).isEqualTo( uuid );
    }

    @Test
    @DisplayName("sem repositório injetado (teste unitário) o mapeamento continua respondendo")
    void semRepositorio_naoQuebra( )
    {
        Lembrete lembrete = new Lembrete( );
        lembrete.setSessaoUuid( UUID.randomUUID( ) );

        assertThat( mapper.toEntity( lembrete ).getSessao( ) ).isNull( );
    }

    @Test
    @DisplayName("updateEntity não toca a sessão: a posse não muda numa atualização de conteúdo")
    void updateEntity_preservaASessao( )
    {
        SessaoAnonimaEntity dona = sessao( UUID.randomUUID( ) );
        LembreteEntity entity = LembreteExample.entity( );
        entity.setSessao( dona );

        mapper.updateEntity( LembreteExample.simplesInput( ), entity );

        assertThat( entity.getSessao( ) ).isSameAs( dona );
    }
}
