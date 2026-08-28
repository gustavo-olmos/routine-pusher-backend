package com.routine.pusher.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Categoria é o único recurso da API sem dono: a lista é única e todos os visitantes enxergam a
 * mesma. Enquanto a escrita esteve aberta, qualquer um podia enchê-la (verificado: três categorias
 * criadas sem autenticação, visíveis para outro visitante) ou apagar as que ninguém usava.
 *
 * <p>Estes casos fixam a decisão no lugar onde ela mora — a cadeia de segurança do perfil padrão —
 * para que um refactor futuro em {@code SecurityConfig} não a desfaça em silêncio.</p>
 */
class EscritaDeCategoriaTest
{
    @Test
    @DisplayName("o matcher de categoria cobre a coleção e o item")
    void matcherCobreColecaoEItem( )
    {
        // /api/v1/categoria/** no Spring Security casa tanto a coleção quanto /{id}.
        assertThat( SecurityConfig.MATCHER_CATEGORIA ).isEqualTo( "/api/v1/categoria/**" );
    }

    @Test
    @DisplayName("categoria fica dentro do prefixo de API, e não numa cadeia à parte")
    void categoriaEstaSobOPrefixoDaApi( )
    {
        String prefixo = SecurityConfig.MATCHER_API.replace( "/**", "" );

        assertThat( SecurityConfig.MATCHER_CATEGORIA ).startsWith( prefixo );
    }

    @Test
    @DisplayName("o health check segue público: exigir credencial faz a plataforma reiniciar o container")
    void healthSeguePublico( )
    {
        assertThat( SecurityConfig.ROTAS_PUBLICAS ).contains( "/actuator/health" );
    }
}
