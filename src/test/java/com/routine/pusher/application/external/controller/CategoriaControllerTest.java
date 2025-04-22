package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.service.interfaces.CategoriaService;
import com.routine.pusher.data.example.input.CategoriaInputDTOExample;
import com.routine.pusher.data.example.output.CategoriaOutputDTOExample;
import com.routine.pusher.data.model.dto.CategoriaInputDTO;
import com.routine.pusher.data.model.dto.CategoriaOutputDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CategoriaControllerTest
{
    @Mock
    private CategoriaService service;

    @Autowired
    private TestRestTemplate restTemplate;

    @InjectMocks
    private CategoriaController controller;

    @Test
    @DisplayName("Testa o retorno do endpoint de adicionar categoria num cenário ideal")
    void testaSalvar_01( )
    {
        // 1. Arrange
        CategoriaInputDTO input           = CategoriaInputDTOExample.simples( );
        CategoriaOutputDTO outputEsperado = CategoriaOutputDTOExample.simples( );

        doReturn( outputEsperado ).when( service ).adicionar( any( CategoriaInputDTO.class ) );

        HttpHeaders headers = new HttpHeaders( );
        headers.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<CategoriaInputDTO> request = new HttpEntity<>( input, headers );

        // 2. Act
        ResponseEntity<CategoriaOutputDTO> response = restTemplate
                .postForEntity( "/categoria", request, CategoriaOutputDTO.class );

        CategoriaOutputDTO responseBody = response.getBody();
        assertNotNull(responseBody);

        //3. Assert
        assertAll(
                ( ) -> assertThat( response.getStatusCode( ).value( ) ).isEqualTo( 200 ),
                ( ) -> assertEquals( MediaType.APPLICATION_JSON, response.getHeaders( ).getContentType( ) ),
                ( ) -> assertEquals( outputEsperado.nome( ), responseBody.nome( ) ),
                ( ) -> assertEquals( outputEsperado.cor( ), responseBody.cor( ) ),
                ( ) -> assertEquals( outputEsperado.fatorOrdem( ), responseBody.fatorOrdem( ) ) );
    }
}
