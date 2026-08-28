package com.routine.pusher.application.external.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sem {@code required} no schema, o modelo pode omitir qualquer campo — e omitia: a primeira frase
 * real voltou sem {@code categoriaId} e sem {@code notificacao}. Estes casos travam que as mesmas
 * anotações de Bean Validation que recusam o pedido de um humano também obrigam o modelo.
 */
class ObrigatoriedadeNoSchemaTest
{
    private final JsonNode schema = new ContratoLembreteIA( ).schema( );

    private List<String> obrigatoriosDe( JsonNode no )
    {
        JsonNode required = no.get( "required" );

        return required == null ? List.of( )
                : required.findValuesAsText( "" ).isEmpty( )
                        ? java.util.stream.StreamSupport.stream( required.spliterator( ), false )
                                .map( JsonNode::asText ).toList( )
                        : List.of( );
    }

    @Test
    @DisplayName("@NotNull e @NotBlank do DTO viram campos obrigatórios para o modelo")
    void anotacoesViramObrigatoriedade( )
    {
        assertThat( obrigatoriosDe( schema ) )
                .contains( "titulo", "categoriaId", "notificacao" );
    }

    @Test
    @DisplayName("a obrigatoriedade desce para os objetos aninhados")
    void obrigatoriedadeNosAninhados( )
    {
        JsonNode notificacao = schema.get( "properties" ).get( "notificacao" );

        assertThat( obrigatoriosDe( notificacao ) ).contains( "metodo" );
    }

    @Test
    @DisplayName("campo opcional continua opcional: recorrência não é exigida")
    void opcionalSegueOpcional( )
    {
        assertThat( obrigatoriosDe( schema ) ).doesNotContain( "recorrencia", "descricao" );
    }

    @Test
    @DisplayName("@Size vira maxLength: o teto da coluna passa a valer também para o modelo")
    void sizeViraMaxLength( )
    {
        assertThat( schema.get( "properties" ).get( "titulo" ).get( "maxLength" ).asInt( ) )
                .isEqualTo( 255 );
    }

    @Test
    @DisplayName("nenhum pattern entra: o structured outputs da Anthropic o recusa")
    void semPattern( )
    {
        assertThat( schema.toString( ) ).doesNotContain( "pattern" );
    }
}
