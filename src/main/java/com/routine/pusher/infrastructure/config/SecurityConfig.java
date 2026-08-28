package com.routine.pusher.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
 * Segurança padrão da aplicação. Duas formas de autenticar convivem na mesma cadeia:
 *
 * <ul>
 *   <li><b>Sessão</b> ({@code oauth2Login}): o navegador faz o redirect para o Google e recebe o
 *       cookie {@code JSESSIONID}. É o que sustenta a tela inicial e o Swagger UI.</li>
 *   <li><b>Bearer</b> ({@code oauth2ResourceServer}): qualquer cliente HTTP — Postman, curl, um app
 *       mobile no futuro — manda o {@code id_token} do Google no header {@code Authorization}.</li>
 * </ul>
 *
 * O Bearer é habilitado <b>na cadeia inteira</b>, não endpoint a endpoint: nenhum controller precisa
 * declarar nada para passar a aceitar o header.
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

    private static final String EMISSOR_GOOGLE = "https://accounts.google.com";

    private static final String JWK_SET_URI = "https://www.googleapis.com/oauth2/v3/certs";

    @Bean
    @Profile("!dev")
    public SecurityFilterChain filterChain( HttpSecurity http ) throws Exception
    {
        http.authorizeHttpRequests( authManager ->
             authManager.requestMatchers( ROTAS_PUBLICAS ).permitAll( )
                        .anyRequest( ).authenticated( ) )
            .oauth2Login( oauth -> oauth.defaultSuccessUrl( "/", true ) )
            .oauth2ResourceServer( oauth -> oauth.jwt( Customizer.withDefaults( ) ) )
            // CSRF existe para proteger formulário submetido por navegador com cookie. Chamada de
            // máquina em /api/** se identifica pelo header Authorization, então o token de CSRF só
            // atrapalharia (é a origem do 403 em POST/PUT/DELETE via Postman e via Swagger).
            .csrf( csrf -> csrf.ignoringRequestMatchers( MATCHER_API ) );

        return http.build( );
    }

    /**
     * Decodifica e valida o {@code id_token} do Google. A assinatura é conferida contra as chaves
     * públicas publicadas no JWKS; além do padrão (expiração + emissor), exige que a audiência seja
     * este client-id — sem isso, um token emitido para qualquer outra aplicação Google seria aceito.
     */
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId )
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
