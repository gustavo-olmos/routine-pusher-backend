package com.routine.pusher.application.external.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import com.routine.pusher.example.CategoriaExample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = CategoriaController.class)
public class CategoriaControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CRUDUseCase<CategoriaInputDTO, CategoriaOutputDTO> useCase;

    public static String asJsonString( final Object obj )
    {
        try {
            return new ObjectMapper( ).writeValueAsString( obj );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }


    @Test
    @DisplayName("Testa o retorno do endpoint de adicionar categoria num cenário ideal")
    void testaSalvar_01( ) throws Exception
    {
        // 1. Arrange
        CategoriaInputDTO input = CategoriaExample.inputDTO( );
        CategoriaOutputDTO outputEsperado = CategoriaExample.outputDTO( );

        when( useCase.adicionar( any( CategoriaInputDTO.class ) ) ).thenReturn( outputEsperado );

        // 2. Act
        ResultActions result = mockMvc.perform( post( "/api/v1/categoria" )
                .contentType( MediaType.APPLICATION_JSON )
                .content( asJsonString( input ) ) );

        // 3. Assert
        result.andExpect( status( ).isOk( ) )
              .andExpect( jsonPath( "$.id" ).value( outputEsperado.id( ) ) )
              .andExpect( jsonPath( "$.nome" ).value( outputEsperado.nome( ) ) );
    }
}
