//package com.routine.pusher.services;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.doReturn;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import br.com.sinqia.fundos.sqfaas.frontend.bff.psv.cadbasicos.integrations.ExampleIntegration;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.psv.cadbasicos.model.ExampleInput;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.psv.cadbasicos.model.ExampleOutput;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.psv.cadbasicos.mothers.ExampleInputMother;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.psv.cadbasicos.mothers.ExampleOutputMother;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.psv.cadbasicos.services.impl.ExampleServiceImpl;
//
///**
// *
// * Classe <code>ExampleServiceTest</code>.
// *
// * O objetivo desse teste é validar que a classe de implementação do serviço executa corretamente a sua regra de negócio
// * corretamente, dado a sua entrada e dado que as classes de integração respondem de uma forma esperada.
// * Dito isso, é esperado diversos cenários de teste cobrindo todos os métodos da classe de serviço. Porém, pode ser que
// * em boa parte dos casos seja essas classes tenham um implementação simples de delegação, que levará a um teste
// * simples.
// * O método de testes de serviços é sempre o mesmo:
// * - Criamos um mock para cada integração que necessite ser chamado pela classe de serviço;
// * - Criamos a classe concreta de serviço que queremos testar;
// * - Testamos cada método da classe, para cada condição que o código faça, ou variações de entrada que gerem resultados
// * diferentes por algum processamento na classe;
// *
// */
//@ExtendWith(MockitoExtension.class)
//class ServiceTest
//{
//    @Mock
//    ExampleIntegration integracao;
//
//    /**
//     * Cada método de teste deve ser nomeado como 'test<metodo><sequencia>', conforme exemplo abaixo.
//     * Um detalhe do que está sendo testado deve ser escrito no DisplayName, pois isso que irá aparecer em relatórios e
//     * no eclipse.
//     * Esse método é um exemplo de um caso simples:
//     * - As entradas do método são criadas;
//     * - Os valores de mocks são criadas e aplicados aos métodos mockados;
//     * - Instanciamos a classe a ser testada e executamos o método que desejamos testar;
//     * - Verificamos com asserts se a informação retornada é a esperada;
//     */
//    @Test
//    @DisplayName("Processar: Testa XXX de YYY com ZZZ")
//    void testProcessar01( )
//    {
//        ExampleInput input = ExampleInputMother.simples( );
//        ExampleOutput esperado = ExampleOutputMother.simples( );
//
//        doReturn( esperado ).when( integracao ).processar( input );
//
//        ExampleOutput output = new ExampleServiceImpl( integracao ).process( input );
//
//        assertThat( output ).isEqualTo( esperado );
//    }
//}
