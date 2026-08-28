package com.routine.pusher.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * Segurança padrão da aplicação, com três públicos distintos:
 *
 * <ul>
 *   <li><b>API ({@code /api/**})</b>: aberta no nível do Spring Security — o visitante do demo não
 *       tem login. Quem escopa o que cada um enxerga é a sessão anônima do cookie
 *       ({@code SessaoAnonimaFilter}), e quem contém abuso são as travas de uso por sessão.</li>
 *   <li><b>Sessão Google</b> ({@code oauth2Login}): o redirect para o Google segue protegendo o que
 *       não é API — Swagger UI em particular.</li>
 *   <li><b>Bearer</b> ({@code oauth2ResourceServer}): cliente HTTP com {@code id_token} do Google no
 *       header {@code Authorization}, habilitado na cadeia inteira.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig
{
    /** Tudo sob este prefixo é consumido como API (cliente HTTP), não como navegação de navegador. */
    public static final String MATCHER_API = "/api/**";

    /**
     * O health check e consultado pela plataforma de deploy, sem credencial nenhuma. Se exigir
     * autenticacao, a plataforma conclui que a instancia esta doente e fica reiniciando o container.
     */
    public static final String[] ROTAS_PUBLICAS = { "/", "/actuator/health", "/actuator/health/**" };

    public static final String[] ROTAS_SWAGGER = { "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**" };

    /**
     * Categoria é cenário do demo, não dado do visitante: a lista é única e compartilhada por todos,
     * então escrita anônima aqui deixa qualquer um poluir (ou apagar) o que todo mundo enxerga. As
     * categorias nascem prontas pela migração V5; escrever nelas é operação de dono.
     */
    public static final String MATCHER_CATEGORIA = "/api/v1/categoria/**";

    private static final String EMISSOR_GOOGLE = "https://accounts.google.com";

    private static final String JWK_SET_URI = "https://www.googleapis.com/oauth2/v3/certs";

    @Bean
    @Profile("!dev")
    public SecurityFilterChain filterChain( HttpSecurity http ) throws Exception
    {
        http.authorizeHttpRequests( authManager ->
             authManager.requestMatchers( ROTAS_PUBLICAS ).permitAll( )
                        // Ler categoria é necessário para o visitante escolher uma; criar, alterar
                        // e apagar mexem na lista de todos, então exigem dono autenticado. A ordem
                        // importa: estas regras precisam vir ANTES do permitAll de /api/**.
                        .requestMatchers( HttpMethod.POST, MATCHER_CATEGORIA ).authenticated( )
                        .requestMatchers( HttpMethod.PUT, MATCHER_CATEGORIA ).authenticated( )
                        .requestMatchers( HttpMethod.DELETE, MATCHER_CATEGORIA ).authenticated( )
                        // Sem login no resto da API: a identidade do visitante é a sessão anônima,
                        // resolvida pelo filtro de cookie fora desta cadeia.
                        .requestMatchers( MATCHER_API ).permitAll( )
                        .anyRequest( ).authenticated( ) )
            // Aplica o CorsConfigurationSource (ver CorsConfig) antes das regras de autorização:
            // sem isto o preflight OPTIONS de outra origem morreria na cadeia.
            .cors( Customizer.withDefaults( ) )
            .oauth2Login( oauth -> oauth.defaultSuccessUrl( "/", true ) )
            .oauth2ResourceServer( oauth -> oauth.jwt( Customizer.withDefaults( ) ) )
            // CSRF fora de /api/**: a proteção lá é o SameSite=Lax do cookie de sessão — site de
            // terceiro não consegue anexá-lo num POST cross-site, e os GETs são somente leitura.
            // O token de CSRF só atrapalharia Postman e Swagger (403 em POST/PUT/DELETE).
            .csrf( csrf -> csrf.ignoringRequestMatchers( MATCHER_API ) );

        return http.build( );
    }

    /**
     * Decodifica e valida o {@code id_token} do Google. A assinatura é conferida contra as chaves
     * públicas publicadas no JWKS; além do padrão (expiração + emissor), exige que a audiência seja
     * este client-id — sem isso, um token emitido para qualquer outra aplicação Google seria aceito.
     * <p>Com o client-id vazio (demo sem login configurado) o validador de audiência nunca casa,
     * então todo Bearer é recusado — falha fechada, que é o lado certo para errar.</p>
     */
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.client.registration.google.client-id:}") String clientId )
    {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri( JWK_SET_URI ).build( );
        decoder.setJwtValidator( validadorDeToken( clientId ) );

        return decoder;
    }

    private OAuth2TokenValidator<Jwt> validadorDeToken( String clientId )
    {
        OAuth2TokenValidator<Jwt> padrao = JwtValidators.createDefaultWithIssuer( EMISSOR_GOOGLE );

        OAuth2TokenValidator<Jwt> audiencia = new JwtClaimValidator<List<String>>(
                JwtClaimNames.AUD, audiencias -> audiencias != null && audiencias.contains( clientId ) );

        return new DelegatingOAuth2TokenValidator<>( padrao, audiencia );
    }
}
