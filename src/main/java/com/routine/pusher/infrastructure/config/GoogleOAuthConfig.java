package com.routine.pusher.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

/**
 * Registra o login do Google — e só existe quando há credencial para registrar.
 *
 * <p>O caminho natural seria declarar {@code spring.security.oauth2.client.registration.google} no
 * YAML com default vazio, e foi o que o projeto fazia. Não funciona: o Spring Boot valida as
 * registrações <b>declaradas</b> e recusa {@code client-id} vazio, então a chave presente no YAML
 * já basta para derrubar o boot. O default vazio parecia desligar o login; na verdade trocava
 * "sem login" por "não sobe". O sintoma só aparece em ambiente sem {@code .env} — o container foi
 * o primeiro.</p>
 *
 * <p>Aqui a condição é explícita e a ausência de credencial produz o que se esperava desde o
 * começo: nenhuma registração, nenhum {@code oauth2Login}, e a API do demo funcionando inteira.</p>
 *
 * <p>{@link ConditionalOnExpression} em vez de {@code @ConditionalOnProperty} porque este último
 * não distingue vazio de ausente: com {@code havingValue} omitido ele aceita qualquer valor que
 * não seja {@code false}, e string vazia passaria.</p>
 */
@Configuration
@ConditionalOnExpression("!'${app.google.client-id:}'.isEmpty()")
public class GoogleOAuthConfig
{
    /**
     * {@link CommonOAuth2Provider#GOOGLE} já carrega os endpoints, o JWKS e os escopos padrão
     * ({@code openid}, {@code profile}, {@code email}) — o YAML só repetia o que o Spring Security
     * já sabe sobre o Google.
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${app.google.client-id}") String clientId,
            @Value("${app.google.client-secret}") String clientSecret )
    {
        return new InMemoryClientRegistrationRepository(
                CommonOAuth2Provider.GOOGLE.getBuilder( "google" )
                                           .clientId( clientId )
                                           .clientSecret( clientSecret )
                                           .build( ) );
    }
}
