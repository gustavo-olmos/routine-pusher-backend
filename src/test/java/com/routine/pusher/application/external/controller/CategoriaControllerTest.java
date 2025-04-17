package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.service.interfaces.CategoriaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class CategoriaControllerTest
{
    @Mock
    private CategoriaService service;

    @InjectMocks
    private CategoriaController controller;

    @Test
    @DisplayName("Testa o retorno do endpoint de salvar categoria num cenário ideal")
    void testaSalvar( )
    {
//        CategoriaInputDTO input           = CategoriaInputDTOMother.simples( );
//        CategoriaOutputDTO outputEsperado = CategoriaOutputDTOMother.simples( );
//
//        doReturn( outputEsperado ).when( service ).process( any( CategoriaInputDTO.class ) );
//
//        Response response = target( "/categoria" ).request( MediaType.APPLICATION_JSON )
//                .post( Entity.entity( input, MediaType.APPLICATION_JSON ) );
//
//        CategoriaOutputDTO output = response.readEntity( CategoriaOutputDTO.class );
//
//        assertAll(
//                ( ) -> assertEquals( 200, response.getStatus( ) ),
//                ( ) -> assertEquals( "application/json", response.getMediaType( ).toString( ) );
//                ( ) -> assertEquals( outputEsperado.cost( ), output.cost( ) ),
//                ( ) -> assertEquals( outputEsperado.date( ), output.date( ) ),
//                ( ) -> assertEquals( outputEsperado.datetime( ), output.datetime( ) ) );
    }
}
