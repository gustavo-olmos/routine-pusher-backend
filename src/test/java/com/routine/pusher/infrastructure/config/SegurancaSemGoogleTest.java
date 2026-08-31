package com.routine.pusher.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O demo é anônimo: o login do Google é acessório e nenhum ambiente de produção precisa carregar
 * aquelas credenciais. Este caso fixa isso.
 *
 * <p>Existe porque a versão anterior <b>não</b> subia sem elas, e ninguém percebeu: o
 * {@code application.yml} declarava a registração do Google com default vazio acreditando que isso
 * a desligava, quando na verdade o Spring Boot valida toda registração declarada e recusa
 * {@code client-id} vazio. Localmente o {@code .env} preenchia, e o
 * {@code src/test/resources/application.yml} preenchia com {@code test-id} — os dois ambientes que
 * existiam mascaravam a falha. Ela só apareceu no primeiro ambiente sem {@code .env}: o container
 * em produção, que morreu no boot com <i>"Client id of registration 'google' must not be empty"</i>.</p>
 *
 * <p>Daí este teste sobrescrever a credencial para vazio em vez de confiar na configuração de
 * teste: o cenário que quebra é justamente o que nenhum arquivo de configuração representava.</p>
 */
@SpringBootTest(properties = { "app.google.client-id=", "app.google.client-secret=" })
class SegurancaSemGoogleTest
{
    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("a aplicação sobe sem credencial do Google")
    void contextoSobeSemCredencial( )
    {
        assertThat( contexto.getBeanNamesForType( SecurityFilterChain.class ) ).isNotEmpty( );
    }

    @Test
    @DisplayName("sem credencial não há registração de cliente — logo não há oauth2Login para montar")
    void naoRegistraClienteOAuth( )
    {
        // O bean só nasce em GoogleOAuthConfig, que é condicional. Se ele aparecesse aqui, a
        // condição estaria aceitando string vazia — que é exatamente o erro do @ConditionalOnProperty.
        assertThat( contexto.getBeanNamesForType( ClientRegistrationRepository.class ) ).isEmpty( );
    }
}
