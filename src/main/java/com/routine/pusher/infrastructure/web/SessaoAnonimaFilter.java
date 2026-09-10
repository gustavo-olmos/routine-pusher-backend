package com.routine.pusher.infrastructure.web;

import com.routine.pusher.core.domain.categoria.CategoriaPadrao;
import com.routine.pusher.core.domain.categoria.CategoriaRepository;
import com.routine.pusher.core.domain.sessao.SessaoAnonima;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaEntity;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaRepository;
import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * Garante que toda requisição de {@code /api/**} chegue ao resto da aplicação com uma sessão
 * anônima resolvida: reaproveita a do cookie quando ela ainda vale, cria uma nova quando não há ou
 * quando expirou. O UUID resolvido é publicado como atributo de request — é de lá que o
 * {@link SessaoAtualPort} responde.
 *
 * <p>O cookie é {@code HttpOnly} (script não lê a identidade) e {@code SameSite=Lax} — que é também
 * a defesa de CSRF deste modelo: site de terceiro não consegue anexar o cookie num POST cross-site,
 * e os GETs da API são somente leitura. Front e API precisam morar no mesmo domínio registrável
 * (ex.: {@code seudominio.com} e {@code api.seudominio.com}) para o Lax valer.</p>
 */
public class SessaoAnonimaFilter extends OncePerRequestFilter
{
    public static final String COOKIE_SESSAO = "RP_SESSAO";

    /**
     * O cookie dura mais que a janela de inatividade de propósito: quem manda é o servidor. Cookie
     * vivo com sessão já expirada só significa que a próxima visita troca por uma sessão nova.
     * <p>
     * O inverso é que seria ruim, e era o caso antes: com cookie de 1 dia, o visitante que voltasse
     * no segundo dia perderia a identidade enquanto a sessão ainda existia no banco — os lembretes
     * dele continuariam lá, invisíveis, até a faxina levá-los.
     */
    private static final Duration VALIDADE_COOKIE = Duration.ofDays( 7 );

    /**
     * Renovar {@code ultimo_acesso} em toda requisição viraria um UPDATE por clique. Com janela de
     * inatividade medida em dias, adiar a renovação por uma hora não chega perto de expirar alguém
     * cedo — e derruba a escrita repetida, que num Postgres de free tier é o recurso escasso.
     */
    static final Duration INTERVALO_RENOVACAO = Duration.ofHours( 1 );

    private final SessaoAnonimaRepository repository;
    private final CategoriaRepository categoriaRepository;

    public SessaoAnonimaFilter( SessaoAnonimaRepository repository, CategoriaRepository categoriaRepository )
    {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain ) throws ServletException, IOException
    {
        LocalDateTime agora = LocalDateTime.now( );

        SessaoAnonimaEntity sessao = sessaoDoCookie( request )
                .filter( s -> !expirada( s, agora ) )
                .orElse( null );

        if( sessao == null )
            sessao = criarSessao( response, request.isSecure( ) );
        else
            renovarAcesso( sessao, agora );

        request.setAttribute( SessaoAtualPort.ATRIBUTO_REQUISICAO, sessao.getUuid( ) );

        filterChain.doFilter( request, response );
    }

    private Optional<SessaoAnonimaEntity> sessaoDoCookie( HttpServletRequest request )
    {
        Cookie[] cookies = request.getCookies( );
        if( cookies == null ) return Optional.empty( );

        return Arrays.stream( cookies )
                .filter( c -> COOKIE_SESSAO.equals( c.getName( ) ) )
                .findFirst( )
                .flatMap( c -> uuidValido( c.getValue( ) ) )
                .flatMap( repository::findByUuid );
    }

    /** Valor de cookie é entrada externa como qualquer outra: malformado é ausente, não erro. */
    private Optional<UUID> uuidValido( String valor )
    {
        try {
            return Optional.of( UUID.fromString( valor ) );
        }
        catch ( IllegalArgumentException | NullPointerException e ) {
            return Optional.empty( );
        }
    }

    /**
     * Sessão expirada não é reaproveitada nem apagada aqui: quem apaga (junto com lembretes e
     * agendamentos) é a faxina, que sabe desmontar tudo. O filtro só a trata como inexistente.
     */
    private boolean expirada( SessaoAnonimaEntity sessao, LocalDateTime agora )
    {
        return sessao.getUltimoAcesso( ) == null
                || sessao.getUltimoAcesso( ).plus( SessaoAnonima.JANELA_INATIVIDADE ).isBefore( agora );
    }

    private SessaoAnonimaEntity criarSessao( HttpServletResponse response, boolean https )
    {
        SessaoAnonima nova = new SessaoAnonima( );

        SessaoAnonimaEntity entidade = new SessaoAnonimaEntity( );
        entidade.setUuid( nova.getUuid( ) );
        entidade.setDataCriacao( nova.getDataCriacao( ) );
        entidade.setUltimoAcesso( nova.getUltimoAcesso( ) );

        entidade = repository.save( entidade );

        semearCategoriasPadrao( entidade );

        // secure acompanha a conexão em vez de ser fixo: atrás do proxy da plataforma o
        // forward-headers-strategy já faz isSecure() enxergar o HTTPS original; em localhost sem
        // TLS, um cookie Secure simplesmente nunca voltaria.
        ResponseCookie cookie = ResponseCookie.from( COOKIE_SESSAO, entidade.getUuid( ).toString( ) )
                .httpOnly( true )
                .secure( https )
                .path( "/" )
                .maxAge( VALIDADE_COOKIE )
                .sameSite( "Lax" )
                .build( );

        response.addHeader( HttpHeaders.SET_COOKIE, cookie.toString( ) );

        return entidade;
    }

    /**
     * A lista de categorias nasce junto com a sessão porque ela pertence à sessão: sem isto o
     * visitante chegaria a uma lista vazia e não conseguiria criar o primeiro lembrete, já que
     * {@code categoriaId} é obrigatório.
     * <p>
     * Semear aqui, e não numa migração, é consequência do modelo — as categorias do demo eram
     * globais e nasciam na V5; agora dependem de uma sessão que só existe em tempo de execução.
     * As duas são linhas comuns da lista: podem ser renomeadas, recoloridas ou apagadas.
     */
    private void semearCategoriasPadrao( SessaoAnonimaEntity sessao )
    {
        categoriaRepository.saveAll(
                Arrays.stream( CategoriaPadrao.values( ) )
                      .map( padrao -> padrao.paraSessao( sessao ) )
                      .toList( ) );
    }

    private void renovarAcesso( SessaoAnonimaEntity sessao, LocalDateTime agora )
    {
        boolean recente = sessao.getUltimoAcesso( ) != null
                && sessao.getUltimoAcesso( ).plus( INTERVALO_RENOVACAO ).isAfter( agora );
        if( recente ) return;

        sessao.setUltimoAcesso( agora );
        repository.save( sessao );
    }
}
