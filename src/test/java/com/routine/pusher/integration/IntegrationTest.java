//package com.routine.pusher.integration;
//
//import static org.junit.jupiter.api.Assertions.assertAll;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doReturn;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.Spy;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import br.com.sinqia.fundos.sqfaas.frontend.bff.psv.cadbasicos.integrations.sqfaas.clients.ExemploClient;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.psv.cadbasicos.integrations.sqfaas.dto.ExemploDto;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.psv.cadbasicos.integrations.sqfaas.dto.ExemploRespostaDto;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.psv.cadbasicos.integrations.sqfaas.impl.ExampleIntegrationImpl;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.psv.cadbasicos.mappers.ExampleMapperImpl;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.psv.cadbasicos.model.ExampleInput;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.psv.cadbasicos.model.ExampleOutput;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.psv.cadbasicos.mothers.ExampleInputMother;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.psv.cadbasicos.mothers.ExampleOutputMother;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.psv.cadbasicos.mothers.ExemploRespostaDtoMother;
//
///**
// * Classe <code>ExampleIntegrationTest</code>.
// *
// * O objetivo desse tipo de teste é testar se o método de integração faz o processamento correto.
// * Todo o processamento de mapeamento de DTO é feito na integração, portanto, por tabela, já temos os testes dos mappers
// * realizados.
// * O método de testes de integração é sempre o mesmo:
// * - Criamos um mock para cada cliente REST que necessite ser chamado pela classe de integração;
// * - Criamos um spy para cada classe mapper concreta que necessite ser chamada pela classe de integração;
// * - Criamos a classe concreta de integração que queremos testar;
// * - Testamos cada método da classe, para cada condição que o código faça, ou variações de entrada que gerem resultados
// * diferentes por algum processamento na classe;
// */
//@ExtendWith(MockitoExtension.class)
//class IntegrationTest
//{
//    @Mock
//    ExemploClient client;
//
//    @Spy
//    ExampleMapperImpl mapper;
//
//    /**
//     * Cada método de teste deve ser nomeado como 'test<metodo><sequencia>', conforme exemplo abaixo.
//     * Um detalhe do que está sendo testado deve ser escrito no DisplayName, pois isso que irá aparecer em relatórios e
//     * no eclipse.
//     * Esse método é um exemplo de um caso simples:
//     * - As entradas do método são criadas;
//     * - Os valores de mocks são criadas e aplicados aos métodos mockados;
//     * - Instanciamos a classe com os mocks e executamos o método que desejamos testar;
//     * - Verificamos com asserts se a informação retornada é a esperada;
//     */
//    @Test
//    @DisplayName("Processar: Testa XXX de YYY com ZZZ")
//    void testProcessar01( )
//    {
//        ExampleInput       input   = ExampleInputMother.simples( );
//        ExemploRespostaDto retorno = ExemploRespostaDtoMother.simples( );
//
//        doReturn( retorno ).when( client ).processar( any( ExemploDto.class ) );
//
//        ExampleOutput output   = new ExampleIntegrationImpl( client, mapper ).processar( input );
//        ExampleOutput esperado = ExampleOutputMother.simples( );
//
//        /*
//         * Porque não é um assert único? Porque a ideia aqui é testar que todos os dados são os esperados e alertar os
//         * que não estão iguais.
//         */
//        assertAll(
//                ( ) -> assertEquals( esperado.response( ), output.response( ) ),
//                ( ) -> assertEquals( esperado.size( ), output.size( ) ),
//                ( ) -> assertEquals( esperado.cost( ), output.cost( ) ),
//                ( ) -> assertEquals( esperado.date( ), output.date( ) ),
//                ( ) -> assertEquals( esperado.datetime( ), output.datetime( ) ) );
//    }
//}
