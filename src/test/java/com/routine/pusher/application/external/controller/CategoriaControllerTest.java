package com.routine.pusher.application.external.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import com.routine.pusher.example.CategoriaExample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = CategoriaController.class)
@AutoConfigureMockMvc(addFilters = false) // isola a camada web: segurança/CSRF são testados à parte
public class CategoriaControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CRUDUseCase<CategoriaInputDTO, CategoriaOutputDTO, Long> useCase;

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
    void test_Salvar_01( ) throws Exception
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

    /**
     * Guarda a regressão do 500: {@code cor} é {@code nullable = false} na entidade, mas o DTO não
     * exigia nada, então a requisição chegava ao banco e estourava violação de restrição. Tem que
     * parar na validação, antes de qualquer acesso a dados.
     */
    @Test
    @DisplayName("Adicionar categoria sem cor é rejeitado na validação, não no banco")
    void test_Salvar_02( ) throws Exception
    {
        // 1. Arrange
        CategoriaInputDTO semCor = new CategoriaInputDTO( "Importante", null, 1 );

        // 2. Act
        ResultActions result = mockMvc.perform( post( "/api/v1/categoria" )
                .contentType( MediaType.APPLICATION_JSON )
                .content( asJsonString( semCor ) ) );

        // 3. Assert
        result.andExpect( status( ).isBadRequest( ) );
        verify( useCase, never( ) ).adicionar( any( CategoriaInputDTO.class ) );
    }
}
