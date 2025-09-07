package com.routine.pusher.application.external.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.application.usecase.ConcluirUseCase;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.example.LembreteExample;
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
@ContextConfiguration(classes = LembreteController.class)
class LembreteControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CRUDUseCase<LembreteInputDTO, LembreteOutputDTO> useCase;

    @MockBean
    private ConcluirUseCase concluirUseCase;


    public static String asJsonString( final Object obj )
    {
        try {
            return new ObjectMapper( ).writeValueAsString( obj );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    @Test
    @DisplayName("Testa o retorno do endpoint de salvar lembrete num cenário ideal")
    void test_Salvar_01( ) throws Exception
    {
//        // 1. Arrange
//        LembreteInputDTO input           = LembreteExample.simplesInput( );
//        LembreteOutputDTO outputEsperado = LembreteExample.simplesOutput( );
//
//        when( useCase.adicionar( any( LembreteInputDTO.class ) ) ).thenReturn( outputEsperado );
//
//        // 2. Act
//        ResultActions result = mockMvc.perform( post( "/api/v1/lembrete" )
//                .contentType( MediaType.APPLICATION_JSON )
//                .content( asJsonString( input ) ) );
//
//        //3. Assert
//        result.andExpect( status( ).isOk( ) )
//                .andExpect( jsonPath( "$.id" ).value( outputEsperado.id( ) ) )
//                .andExpect( jsonPath( "$.titulo" ).value( outputEsperado.titulo( ) ) )
//                .andExpect( jsonPath( "$.descricao" ).value( outputEsperado.descricao( ) ) );
    }
}
