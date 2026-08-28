package com.routine.pusher.application.external.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.routine.pusher.core.domain.categoria.Categoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A regressão que motivou tudo isto: a versão OpenAI mandava ao modelo a string
 * {@code "com.routine...LembreteInputDTO"} — o nome da classe, 62 caracteres — e o modelo adivinhava
 * o formato inteiro. Estes casos fixam que o contrato derivado carrega a estrutura de verdade, que
 * acompanha o DTO sozinho, e que respeita o que os dois provedores aceitam.
 */
class ContratoLembreteIATest
{
    private final ContratoLembreteIA contrato = new ContratoLembreteIA( );

    private final String schema = contrato.schema( ).toString( );

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
    @DisplayName("sem $ref nem pattern: o subset comum aos dois provedores")
    void respeitaOSubsetDosDoisProvedores( )
    {
        // $ref porque o dialeto do Gemini é OpenAPI e não resolve referência;
        // pattern porque structured outputs da Anthropic o recusa.
        assertThat( schema )
                .doesNotContain( "$ref" )
                .doesNotContain( "$defs" )
                .doesNotContain( "pattern" )
                .doesNotContain( "$schema" );
    }

    @Test
    @DisplayName("cada chamador recebe cópia: ajustar ao dialeto de um provedor não afeta o outro")
    void schemaEhCopiaDefensiva( )
    {
        ObjectNode primeiro = contrato.schema( );
        primeiro.remove( "properties" );

        assertThat( contrato.schema( ).has( "properties" ) ).isTrue( );
    }

    @Test
    @DisplayName("o contexto leva relógio do usuário, categorias e frase — a frase por último, como dado")
    void contextoCompleto( )
    {
        Categoria categoria = new Categoria( );
        categoria.setId( 7L );
        categoria.setNome( "Trabalho" );

        String contexto = contrato.montarContexto( "beber água a cada 2 horas",
                LocalDateTime.of( 2026, 8, 28, 14, 30 ), List.of( categoria ) );

        assertThat( contexto ).contains( "2026-08-28T14:30" )
                              .contains( "sexta-feira" )
                              .contains( "id=7 nome=Trabalho" );
        assertThat( contexto ).endsWith( "Frase do usuário: beber água a cada 2 horas" );
    }

    @Test
    @DisplayName("sem categorias cadastradas o contexto não mente: diz que não há")
    void contextoSemCategorias( )
    {
        String contexto = contrato.montarContexto( "algo", LocalDateTime.now( ), List.of( ) );

        assertThat( contexto ).contains( "(nenhuma cadastrada)" );
    }

    @Test
    @DisplayName("o prompt é constante: nada acumula entre chamadas")
    void promptEhConstante( )
    {
        String primeira = contrato.promptDeSistema( );
        contrato.montarContexto( "frase de um usuário", LocalDateTime.now( ), List.of( ) );

        assertThat( contrato.promptDeSistema( ) )
                .isEqualTo( primeira )
                .doesNotContain( "frase de um usuário" );
    }
}
