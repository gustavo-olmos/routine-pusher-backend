package com.routine.pusher.infrastructure.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
    @Bean
    public SecurityFilterChain filterChain( HttpSecurity http ) throws Exception
    {
        http.authorizeHttpRequests( authManager ->
             authManager.requestMatchers( "/" ).permitAll( )
                        .anyRequest( ).authenticated( ) )
            .oauth2Login( oauth -> oauth.defaultSuccessUrl( "/", true ) );

        return http.build();
    }
}
