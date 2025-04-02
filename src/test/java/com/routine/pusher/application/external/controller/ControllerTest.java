package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.external.controller.LembreteController;
import com.routine.pusher.application.service.interfaces.LembreteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class ControllerTest
{
    @Mock
    private LembreteService service;

    @InjectMocks
    private LembreteController controller;

    protected Class<?> getControllerClass( )
    {
        return LembreteController.class;
    }

    protected List<Object> getBeans( )
    {
        return List.of( controller, service );
    }

    @Test
    void testaRetornoProcess( )
    {
//        LembreteInputDTO input           = LembreteInputDTOMother.simples( );
//        LembreteOutputDTO outputEsperado = LembreteOutputDTOMother.simples( );
//
//        doReturn( outputEsperado ).when( service ).process( any( LembreteInputDTO.class ) );
//
//        Response response = target( "/lembrete" ).request( MediaType.APPLICATION_JSON )
//                .post( Entity.entity( input, MediaType.APPLICATION_JSON ) );
//
//        LembreteOutputDTO output = response.readEntity( LembreteOutputDTO.class );
//
//        assertAll(
//                ( ) -> assertEquals( 200, response.getStatus( ) ),
//                ( ) -> assertEquals( "application/json", response.getMediaType( ).toString( ) );
//                ( ) -> assertEquals( outputEsperado.cost( ), output.cost( ) ),
//                ( ) -> assertEquals( outputEsperado.date( ), output.date( ) ),
//                ( ) -> assertEquals( outputEsperado.datetime( ), output.datetime( ) ) );
    }

    @Test
    void testaRetornoListOk( )
    {
//        Response response = target( "/lembrete" ).request( MediaType.APPLICATION_JSON ).get( Response.class );
//
//        assertAll(
//                ( ) -> assertEquals( 200, response.getStatus( ) ),
//                ( ) -> assertEquals( "application/json", response.getMediaType( ).toString( ) ) );
    }
}
