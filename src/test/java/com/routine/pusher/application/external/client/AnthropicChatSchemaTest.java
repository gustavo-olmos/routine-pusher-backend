package com.routine.pusher.application.external.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.routine.pusher.core.domain.categoria.Categoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A regressão que motivou tudo isto: a versão OpenAI mandava ao modelo a string
 * {@code "com.routine...LembreteInputDTO"} — o nome da classe, 62 caracteres — e o modelo adivinhava
 * o formato inteiro. Estes casos fixam que o schema derivado carrega o contrato de verdade, que ele
 * acompanha o DTO (campos recentes presentes) e que respeita o subset aceito pela API.
 */
class AnthropicChatSchemaTest
{
    private final AnthropicChatClient client = new AnthropicChatClient(
            "chave-de-teste", "claude-haiku-4-5", new ObjectMapper( ).registerModule( new JavaTimeModule( ) ) );

    private final String schema = client.formatoDeSaida( ).toString( );

    @Test
    @DisplayName("o schema carrega a estrutura do DTO, não o nome da classe")
    void schemaCarregaAEstrutura( )
    {
        assertThat( schema )
                .contains( "titulo" )
                .contains( "categoriaId" )
                .contains( "recorrencia" )
                .contains( "notificacao" )
                .doesNotContain( "com.routine.pusher" );
    }

    @Test
    @DisplayName("o schema acompanha o DTO: campos adicionados depois aparecem sem ninguém lembrar deles")
    void schemaAcompanhaODto( )
    {
        // politicaDiaUtil e datasEspecificadas entraram em fases distintas do projeto — se o schema
        // fosse texto à mão, eram os candidatos a faltar.
        assertThat( schema )
                .contains( "politicaDiaUtil" )
                .contains( "datasEspecificadas" )
                .contains( "diasFixosNoMes" );
    }

    @Test
    @DisplayName("enums viram lista fechada de valores: o modelo não tem como inventar um dia da semana")
    void enumsViramListaFechada( )
    {
        assertThat( schema )
                .contains( "SEGUNDA" )
                .contains( "PULAR" )
                .contains( "ANTECIPAR" );
    }

    @Test
    @DisplayName("as descrições dos campos viajam junto — documentação e contrato são a mesma coisa")
    void descricoesViajamNoSchema( )
    {
        assertThat( schema ).contains( "categorias existentes" );
    }

    @Test
    @DisplayName("datas são string simples: o subset de structured outputs não aceita 'pattern'")
    void datasSaoStringSemPattern( )
    {
        assertThat( schema ).doesNotContain( "pattern" );
    }

    @Test
    @DisplayName("o contexto leva relógio do usuário, categorias e frase — a frase por último, como dado")
    void contextoCompleto( )
    {
        Categoria categoria = new Categoria( );
        categoria.setId( 7L );
        categoria.setNome( "Trabalho" );

        String contexto = client.montarContexto( "beber água a cada 2 horas",
                LocalDateTime.of( 2026, 8, 28, 14, 30 ), List.of( categoria ) );

        assertThat( contexto ).contains( "2026-08-28T14:30" )
                              .contains( "sexta-feira" )
                              .contains( "id=7 nome=Trabalho" );
        assertThat( contexto ).endsWith( "Frase do usuário: beber água a cada 2 horas" );
    }
}
