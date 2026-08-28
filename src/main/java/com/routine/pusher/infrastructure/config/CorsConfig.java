package com.routine.pusher.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * O front mora em origem própria (Angular em dev, o domínio do site em produção) e chama a API com
 * cookie — logo {@code allowCredentials}, e logo nada de {@code *} em origem: o navegador recusa a
 * combinação, e o CORS com credencial exige origem explícita mesmo.
 *
 * <p>CORS não substitui a segurança da sessão: ele só decide que origens de <i>navegador</i> podem
 * ler as respostas. O escopo do que cada visitante enxerga continua sendo o cookie SameSite=Lax —
 * ver {@code SessaoAnonimaFilter}.</p>
 */
@Configuration
public class CorsConfig
{
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") List<String> origens )
    {
        CorsConfiguration config = new CorsConfiguration( );
        config.setAllowedOrigins( origens );
        config.setAllowedMethods( List.of( "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS" ) );
        config.setAllowedHeaders( List.of( "*" ) );
        config.setAllowCredentials( true );

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource( );
        source.registerCorsConfiguration( "/api/**", config );

        return source;
    }
}
