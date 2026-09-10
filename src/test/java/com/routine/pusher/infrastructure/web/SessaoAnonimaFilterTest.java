package com.routine.pusher.infrastructure.web;

import com.routine.pusher.core.domain.categoria.CategoriaEntity;
import com.routine.pusher.core.domain.categoria.CategoriaPadrao;
import com.routine.pusher.core.domain.categoria.CategoriaRepository;
import com.routine.pusher.core.domain.sessao.SessaoAnonima;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaEntity;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaRepository;
import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * O filtro é a porta de entrada da identidade anônima: tudo que chega em /api/** sai daqui com uma
 * sessão resolvida. Os casos fixam as três decisões — criar, reaproveitar, renovar — e o formato do
 * cookie, que é onde mora a segurança do modelo (HttpOnly + SameSite=Lax).
 */
@ExtendWith(MockitoExtension.class)
class SessaoAnonimaFilterTest
{
    @Mock
    private SessaoAnonimaRepository repository;

    @Mock
    private CategoriaRepository categoriaRepository;

    private SessaoAnonimaFilter filtro( )
    {
        return new SessaoAnonimaFilter( repository, categoriaRepository );
    }

    private SessaoAnonimaEntity sessaoCom( UUID uuid, LocalDateTime ultimoAcesso )
    {
        SessaoAnonimaEntity sessao = new SessaoAnonimaEntity( );
        sessao.setId( 1L );
        sessao.setUuid( uuid );
        sessao.setDataCriacao( ultimoAcesso );
        sessao.setUltimoAcesso( ultimoAcesso );
        return sessao;
    }

    @Test
    @DisplayName("primeira visita: cria sessão, emite cookie e publica o uuid para a requisição")
    void primeiraVisita_criaSessaoEEmiteCookie( ) throws Exception
    {
        when( repository.save( any( SessaoAnonimaEntity.class ) ) ).thenAnswer( inv -> inv.getArgument( 0 ) );

        MockHttpServletRequest request = new MockHttpServletRequest( );
        MockHttpServletResponse response = new MockHttpServletResponse( );

        filtro( ).doFilter( request, response, new MockFilterChain( ) );

        String cookie = response.getHeader( "Set-Cookie" );
        assertThat( cookie ).contains( SessaoAnonimaFilter.COOKIE_SESSAO + "=" )
                            .contains( "HttpOnly" )
                            .contains( "SameSite=Lax" )
                            .contains( "Path=/" );
        assertThat( request.getAttribute( SessaoAtualPort.ATRIBUTO_REQUISICAO ) )
                .isInstanceOf( UUID.class );
    }

    @Test
    @DisplayName("sessão nova nasce com as categorias padrão, senão o visitante não cria o primeiro lembrete")
    void sessaoNova_semeiaCategoriasPadrao( ) throws Exception
    {
        when( repository.save( any( SessaoAnonimaEntity.class ) ) ).thenAnswer( inv -> inv.getArgument( 0 ) );

        filtro( ).doFilter( new MockHttpServletRequest( ), new MockHttpServletResponse( ), new MockFilterChain( ) );

        ArgumentCaptor<List<CategoriaEntity>> captor = ArgumentCaptor.forClass( List.class );
        verify( categoriaRepository ).saveAll( captor.capture( ) );

        assertThat( captor.getValue( ) )
                .hasSize( CategoriaPadrao.values( ).length )
                .extracting( CategoriaEntity::getNome )
                .containsExactly( "Importante", "Urgente" );

        assertThat( captor.getValue( ) )
                .allSatisfy( categoria -> assertThat( categoria.getSessao( ) ).isNotNull( ) );
    }

    @Test
    @DisplayName("sessão reaproveitada não ganha categorias de novo")
    void sessaoReaproveitada_naoSemeiaDeNovo( ) throws Exception
    {
        UUID uuid = UUID.randomUUID( );
        when( repository.findByUuid( uuid ) )
                .thenReturn( Optional.of( sessaoCom( uuid, LocalDateTime.now( ) ) ) );

        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.setCookies( new Cookie( SessaoAnonimaFilter.COOKIE_SESSAO, uuid.toString( ) ) );

        filtro( ).doFilter( request, new MockHttpServletResponse( ), new MockFilterChain( ) );

        verify( categoriaRepository, never( ) ).saveAll( any( ) );
    }

    @Test
    @DisplayName("cookie válido de sessão viva: reaproveita sem emitir cookie novo")
    void cookieValido_reaproveitaSessao( ) throws Exception
    {
        UUID uuid = UUID.randomUUID( );
        when( repository.findByUuid( uuid ) )
                .thenReturn( Optional.of( sessaoCom( uuid, LocalDateTime.now( ) ) ) );

        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.setCookies( new Cookie( SessaoAnonimaFilter.COOKIE_SESSAO, uuid.toString( ) ) );
        MockHttpServletResponse response = new MockHttpServletResponse( );

        filtro( ).doFilter( request, response, new MockFilterChain( ) );

        assertThat( response.getHeader( "Set-Cookie" ) ).isNull( );
        assertThat( request.getAttribute( SessaoAtualPort.ATRIBUTO_REQUISICAO ) ).isEqualTo( uuid );
        // Acesso recente: renovar agora seria um UPDATE por clique sem ganho de comportamento.
        verify( repository, never( ) ).save( any( ) );
    }

    @Test
    @DisplayName("sessão parada além do intervalo de renovação: renova o último acesso, que é o relógio da expiração")
    void sessaoParada_renovaUltimoAcesso( ) throws Exception
    {
        UUID uuid = UUID.randomUUID( );
        // Passado o intervalo de renovação e ainda dentro da janela: é exatamente quando renovar.
        SessaoAnonimaEntity sessao = sessaoCom( uuid, LocalDateTime.now( ).minus( SessaoAnonimaFilter.INTERVALO_RENOVACAO ).minusMinutes( 1 ) );
        when( repository.findByUuid( uuid ) ).thenReturn( Optional.of( sessao ) );

        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.setCookies( new Cookie( SessaoAnonimaFilter.COOKIE_SESSAO, uuid.toString( ) ) );

        filtro( ).doFilter( request, new MockHttpServletResponse( ), new MockFilterChain( ) );

        verify( repository ).save( sessao );
        assertThat( sessao.getUltimoAcesso( ) )
                .isAfter( LocalDateTime.now( ).minusMinutes( 1 ) );
    }

    @Test
    @DisplayName("cookie de sessão expirada: vira visitante novo, com identidade nova")
    void sessaoExpirada_criaNova( ) throws Exception
    {
        UUID antiga = UUID.randomUUID( );
        when( repository.findByUuid( antiga ) )
                .thenReturn( Optional.of( sessaoCom( antiga,
                        LocalDateTime.now( ).minus( SessaoAnonima.JANELA_INATIVIDADE ).minusDays( 1 ) ) ) );
        when( repository.save( any( SessaoAnonimaEntity.class ) ) ).thenAnswer( inv -> inv.getArgument( 0 ) );

        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.setCookies( new Cookie( SessaoAnonimaFilter.COOKIE_SESSAO, antiga.toString( ) ) );
        MockHttpServletResponse response = new MockHttpServletResponse( );

        filtro( ).doFilter( request, response, new MockFilterChain( ) );

        assertThat( response.getHeader( "Set-Cookie" ) ).isNotNull( );
        assertThat( request.getAttribute( SessaoAtualPort.ATRIBUTO_REQUISICAO ) ).isNotEqualTo( antiga );
    }

    @Test
    @DisplayName("cookie malformado é entrada externa como outra qualquer: tratado como ausente")
    void cookieMalformado_criaNova( ) throws Exception
    {
        when( repository.save( any( SessaoAnonimaEntity.class ) ) ).thenAnswer( inv -> inv.getArgument( 0 ) );

        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.setCookies( new Cookie( SessaoAnonimaFilter.COOKIE_SESSAO, "nao-sou-um-uuid" ) );
        MockHttpServletResponse response = new MockHttpServletResponse( );

        filtro( ).doFilter( request, response, new MockFilterChain( ) );

        assertThat( response.getHeader( "Set-Cookie" ) ).isNotNull( );
        assertThat( request.getAttribute( SessaoAtualPort.ATRIBUTO_REQUISICAO ) ).isNotNull( );
        verify( repository, never( ) ).findByUuid( any( ) );
    }

    @Test
    @DisplayName("em conexão segura o cookie sobe com Secure; sem TLS local, não — senão nunca voltaria")
    void cookieSecureAcompanhaAConexao( ) throws Exception
    {
        when( repository.save( any( SessaoAnonimaEntity.class ) ) ).thenAnswer( inv -> inv.getArgument( 0 ) );

        MockHttpServletRequest https = new MockHttpServletRequest( );
        https.setSecure( true );
        MockHttpServletResponse resposta = new MockHttpServletResponse( );
        filtro( ).doFilter( https, resposta, new MockFilterChain( ) );
        assertThat( resposta.getHeader( "Set-Cookie" ) ).contains( "Secure" );

        MockHttpServletRequest http = new MockHttpServletRequest( );
        MockHttpServletResponse respostaLocal = new MockHttpServletResponse( );
        filtro( ).doFilter( http, respostaLocal, new MockFilterChain( ) );
        assertThat( respostaLocal.getHeader( "Set-Cookie" ) ).doesNotContain( "Secure" );
    }
}
