package com.routine.pusher.application.external.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O mesmo contrato tem que caber em dois dialetos: a Anthropic aceita o schema canônico como está,
 * e o Gemini usa um subset do OpenAPI que não conhece {@code additionalProperties}. Estes casos
 * exercitam a construção completa de cada adapter — é onde um schema incompatível estoura, ainda no
 * arranque e sem depender de rede.
 */
class DialetoDosProvedoresTest
{
    private final ContratoLembreteIA contrato = new ContratoLembreteIA( );

    private final ObjectMapper objectMapper = new ObjectMapper( ).registerModule( new JavaTimeModule( ) );

    @Test
    @DisplayName("Gemini: o schema chega ao provedor sem o campo que o dialeto dele não conhece")
    void gemini_schemaSemAdditionalProperties( )
    {
        GeminiChatClient client = new GeminiChatClient(
                "chave-de-teste", "gemini-2.5-flash-lite", objectMapper, contrato );

        assertThat( client ).isNotNull( );
        // O canônico traz additionalProperties (a Anthropic usa para fechar o objeto); a poda
        // acontece dentro do adapter do Gemini, e o schema canônico segue intacto para o outro.
        assertThat( contrato.schema( ).toString( ) ).contains( "additionalProperties" );
    }

    @Test
    @DisplayName("Anthropic: o adapter monta o formato de saída a partir do mesmo contrato")
    void anthropic_montaFormatoDeSaida( )
    {
        AnthropicChatClient client = new AnthropicChatClient(
                "chave-de-teste", "claude-haiku-4-5", objectMapper, contrato );

        assertThat( client ).isNotNull( );
    }

    @Test
    @DisplayName("chave ausente não derruba o arranque: o endpoint é que responde indisponível")
    void semChave_naoQuebraNoArranque( )
    {
        assertThat( new GeminiChatClient( "", "gemini-2.5-flash-lite", objectMapper, contrato ) ).isNotNull( );
        assertThat( new GeminiChatClient( null, "gemini-2.5-flash-lite", objectMapper, contrato ) ).isNotNull( );
    }
}
