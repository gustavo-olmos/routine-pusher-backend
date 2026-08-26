package com.routine.pusher.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cadeia de segurança usada <b>apenas no perfil {@code dev}</b> (suba com
 * {@code -Dspring-boot.run.profiles=dev}).
 *
 * <p>Aqui a aplicação inteira fica em whitelist: nenhuma rota exige autenticação e o CSRF está
 * desligado, para que Postman, curl e Swagger UI consigam exercitar qualquer endpoint sem fricção.
 * É deliberadamente permissivo e existe só para teste local.</p>
 *
 * <p>Nada disso afeta o perfil padrão — lá continuam valendo sessão do Google e Bearer, ver
 * {@link SecurityConfig}.</p>
 */
@Configuration
@Profile("dev")
public class DevSecurityConfig
{
    @Bean
    public SecurityFilterChain devFilterChain( HttpSecurity http ) throws Exception
    {
        http.authorizeHttpRequests( authManager -> authManager.anyRequest( ).permitAll( ) )
            .csrf( AbstractHttpConfigurer::disable );

        return http.build( );
    }
}
