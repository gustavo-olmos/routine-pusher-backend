package com.routine.pusher.application.external.client;

import com.openai.client.OpenAIClient;
import com.routine.pusher.data.mapper.LembreteMapperImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Classe <code>ExampleIntegrationTest</code>.
 *
 * O objetivo desse tipo de teste é testar se o método de integração faz o processamento correto. Todo o processamento
 * de mapeamento de DTO é feito na integração, portanto, por tabela, já temos os testes dos mappers realizados.
 *
 * O método de testes de integração é sempre o mesmo:
 * - Criamos um mock para cada cliente REST que necessite ser chamado pela classe de integração;
 * - Criamos um spy para cada classe mapper concreta que necessite ser chamada pela classe de integração;
 * - Criamos a classe concreta de integração que queremos testar;
 * - Testamos cada método da classe, para cada condição que o código faça, ou variações de entrada que gerem resultados
 * diferentes por algum processamento na classe;
 */
@ExtendWith(MockitoExtension.class)
class ClientTest
{
    @Mock
    OpenAIClient client;

    @Spy
    LembreteMapperImpl mapper;

    /**
     * Cada método de teste deve ser nomeado como 'test<metodo><sequencia>', conforme exemplo abaixo.
     * Um detalhe do que está sendo testado deve ser escrito no DisplayName.
     *
     * Esse método é um exemplo de um caso simples:
     * - As entradas do método são criadas;
     * - Os valores de mocks são criadas e aplicados aos métodos mockados;
     * - Instanciamos a classe com os mocks e executamos o método que desejamos testar;
     * - Verificamos com asserts se a informação retornada é a esperada;
     */
    @Test
    @DisplayName("Processar: Testa XXX de YYY com ZZZ")
    void testProcessar01( )
    {
//        LembreteInputDTO input   = LembreteInputDTOMother.simples( );
//        LembreteOutputDTO retorno = LembreteOutputDTOMother.simples( );
//
//        doReturn( retorno ).when( client ).processar( any( LembreteOutputDTO.class ) );
//
//        LembreteOutputDTO output   = new LembreteIntegrationImpl( client, mapper ).processar( input );
//        LembreteOutputDTO esperado = LembreteOutputDTOMother.simples( );
//
//        assertAll(
//                ( ) -> assertEquals( esperado.response( ), output.response( ) ),
//                ( ) -> assertEquals( esperado.size( ), output.size( ) ),
//                ( ) -> assertEquals( esperado.cost( ), output.cost( ) ),
//                ( ) -> assertEquals( esperado.date( ), output.date( ) ),
//                ( ) -> assertEquals( esperado.datetime( ), output.datetime( ) ) );
    }
}
