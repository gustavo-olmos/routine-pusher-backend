package com.routine.pusher.core.domain.sessao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A sessão anônima é o "usuário" do demo: nasce sem cadastro e morre por inatividade. Estes casos
 * fixam o contrato de expiração — é ele que decide o que a faxina pode remover.
 */
class SessaoAnonimaTest
{
    @Test
    @DisplayName("nasce com identidade própria, sem depender do banco")
    void nasceComIdentidadePropria( )
    {
        SessaoAnonima sessao = new SessaoAnonima( );

        assertThat( sessao.getUuid( ) ).isNotNull( );
        assertThat( sessao.getId( ) ).isNull( );
        assertThat( sessao.getUltimoAcesso( ) ).isEqualTo( sessao.getDataCriacao( ) );
    }

    @Test
    @DisplayName("cada sessão é única: dois visitantes nunca colidem")
    void identidadesNaoColidem( )
    {
        assertThat( new SessaoAnonima( ).getUuid( ) ).isNotEqualTo( new SessaoAnonima( ).getUuid( ) );
    }

    @Test
    @DisplayName("dentro da janela de inatividade a sessão continua viva")
    void dentroDaJanela_naoExpira( )
    {
        SessaoAnonima sessao = new SessaoAnonima( );
        sessao.setUltimoAcesso( LocalDateTime.now( ).minusMinutes( 29 ) );

        assertThat( sessao.expirada( LocalDateTime.now( ) ) ).isFalse( );
    }

    @Test
    @DisplayName("passada a janela de inatividade, expira")
    void foraDaJanela_expira( )
    {
        SessaoAnonima sessao = new SessaoAnonima( );
        sessao.setUltimoAcesso( LocalDateTime.now( ).minusMinutes( 31 ) );

        assertThat( sessao.expirada( LocalDateTime.now( ) ) ).isTrue( );
    }

    @Test
    @DisplayName("sem último acesso não há como provar vida: expirada")
    void semUltimoAcesso_expira( )
    {
        SessaoAnonima sessao = new SessaoAnonima( );
        sessao.setUltimoAcesso( null );

        assertThat( sessao.expirada( LocalDateTime.now( ) ) ).isTrue( );
        assertThat( sessao.expiraEm( ) ).isNull( );
    }

    @Test
    @DisplayName("a validade anda com o uso: expiraEm é último acesso + janela")
    void validadeAcompanhaOUso( )
    {
        SessaoAnonima sessao = new SessaoAnonima( );
        LocalDateTime acesso = LocalDateTime.of( 2026, 8, 28, 10, 0 );
        sessao.setUltimoAcesso( acesso );

        assertThat( sessao.expiraEm( ) ).isEqualTo( acesso.plus( SessaoAnonima.JANELA_INATIVIDADE ) );
    }
}
