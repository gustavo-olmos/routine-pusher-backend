//package com.routine.pusher.controller;
//
//import static org.junit.jupiter.api.Assertions.assertAll;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doReturn;
//
//import java.util.List;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import br.com.sinqia.fundos.sqfaas.frontend.bff.template.model.ExampleInput;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.template.model.ExampleOutput;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.template.mothers.ExampleInputMother;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.template.mothers.ExampleOutputMother;
//import br.com.sinqia.fundos.sqfaas.frontend.bff.template.services.ExampleServiceFiltro;
//import br.com.sinqia.fundos.sqfaas.frontend.lib.commons.rest.JerseySpringTest;
//
//import jakarta.ws.rs.client.Entity;
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.Response;
//
//@ExtendWith(MockitoExtension.class)
//@SpringBootTest
//class ControllerTest extends JerseySpringTest
//{
//    @Mock
//    private ExampleServiceFiltro exampleService;
//
//    @InjectMocks
//    private ExampleResource exampleResource;
//
//    protected Class<?> getResourceClass( )
//    {
//        return ExampleResource.class;
//    }
//
//    protected List<Object> getBeans( )
//    {
//        return List.of( exampleResource, exampleService );
//    }
//
//    @Test
//    void testaRetornoProcess( )
//    {
//        ExampleInput  input          = ExampleInputMother.simples( );
//        ExampleOutput outputEsperado = ExampleOutputMother.simples( );
//
//        doReturn( outputEsperado ).when( exampleService ).process( any( ExampleInput.class ) );
//
//        Response response = target( "/example" ).request( MediaType.APPLICATION_JSON )
//                .post( Entity.entity( input, MediaType.APPLICATION_JSON ) );
//
//        ExampleOutput output = response.readEntity( ExampleOutput.class );
//
//        assertAll(
//                ( ) -> assertEquals( 200, response.getStatus( ) ),
//                ( ) -> assertEquals( "application/json", response.getMediaType( ).toString( ) ),
//                ( ) -> assertEquals( outputEsperado.cost( ), output.cost( ) ),
//                ( ) -> assertEquals( outputEsperado.date( ), output.date( ) ),
//                ( ) -> assertEquals( outputEsperado.datetime( ), output.datetime( ) ) );
//    }
//
//    @Test
//    void testaRetornoListOk( )
//    {
//        Response response = target( "/example" ).request( MediaType.APPLICATION_JSON ).get( Response.class );
//
//        assertAll(
//                ( ) -> assertEquals( 200, response.getStatus( ) ),
//                ( ) -> assertEquals( "application/json", response.getMediaType( ).toString( ) ) );
//    }
//}
