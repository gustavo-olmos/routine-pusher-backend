package com.routine.pusher.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cadeia de segurança usada <b>apenas no perfil {@code dev}</b> (suba com
 * {@code -Dspring-boot.run.profiles=dev}). Existe para tornar a API testável fora do navegador,
 * sem afrouxar nada no perfil padrão — ver {@link SecurityConfig}.
 *
 * <p>Diferenças em relação ao padrão:</p>
 * <ul>
 *   <li>Swagger UI liberado, sem exigir login no Google só para abrir a documentação.</li>
 *   <li>CSRF desligado, então Postman/curl conseguem POST/PUT/DELETE.</li>
 *   <li>HTTP Basic com um usuário em memória, para autenticar sem depender do fluxo do Google.</li>
 * </ul>
 *
 * <p>O login por sessão e o Bearer continuam valendo aqui: as três formas convivem.</p>
 */
@Configuration
@Profile("dev")
public class DevSecurityConfig
{
    @Bean
    public SecurityFilterChain devFilterChain( HttpSecurity http ) throws Exception
    {
        http.authorizeHttpRequests( authManager ->
             authManager.requestMatchers( SecurityConfig.ROTAS_PUBLICAS ).permitAll( )
                        .requestMatchers( SecurityConfig.ROTAS_SWAGGER ).permitAll( )
                        .anyRequest( ).authenticated( ) )
            .httpBasic( Customizer.withDefaults( ) )
            .oauth2Login( oauth -> oauth.defaultSuccessUrl( "/", true ) )
            .oauth2ResourceServer( oauth -> oauth.jwt( Customizer.withDefaults( ) ) )
            .csrf( AbstractHttpConfigurer::disable );

        return http.build( );
    }

    /**
     * Usuário local de teste. Vem de {@code DEV_USER}/{@code DEV_PASSWORD} no {@code .env}, com
     * {@code dev/dev} como padrão para o serviço subir mesmo sem configuração nenhuma.
     */
    @Bean
    public UserDetailsService usuarioLocal( @Value("${dev.usuario:dev}") String usuario,
                                            @Value("${dev.senha:dev}") String senha )
    {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder( );

        return new InMemoryUserDetailsManager(
                User.withUsername( usuario )
                    .password( encoder.encode( senha ) )
                    .roles( "DEV" )
                    .build( ) );
    }
}
