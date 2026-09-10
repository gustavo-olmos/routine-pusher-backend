package com.routine.pusher.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sucede o {@code EscritaDeCategoriaTest}, que fixava a exigência de autenticação para escrever em
 * categoria. Aquela restrição existia porque a lista era única e compartilhada — desde a V6 cada
 * sessão tem a própria, e a razão da regra desapareceu junto com ela. O escopo por sessão passou a
 * ser garantido em {@code CategoriaEscopoSessaoTest}.
 *
 * <p>O que sobrevive daquele arquivo é o caso abaixo, que não tem nada a ver com categoria e não
 * podia sumir junto.</p>
 */
class RotasPublicasTest
{
    @Test
    @DisplayName("o health check segue público: exigir credencial faz a plataforma reiniciar o container")
    void healthSeguePublico( )
    {
        assertThat( SecurityConfig.ROTAS_PUBLICAS ).contains( "/actuator/health" );
    }
}
