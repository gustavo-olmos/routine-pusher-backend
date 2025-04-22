package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.service.interfaces.LembreteService;
import com.routine.pusher.data.model.dto.LembreteInputDTO;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.data.example.input.LembreteInputDTOExample;
import com.routine.pusher.data.example.output.LembreteOutputDTOExample;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LembreteControllerTest
{
    @Mock
    private LembreteService service;

    @Autowired
    private TestRestTemplate restTemplate;

    @InjectMocks
    private LembreteController controller;

    @Test
    @DisplayName("Testa o retorno do endpoint de salvar lembrete num cenário ideal")
    void testaSalvar_01( )
    {
        // 1. Arrange
        LembreteInputDTO input           = LembreteInputDTOExample.simples( );
        LembreteOutputDTO outputEsperado = LembreteOutputDTOExample.simples( );

        doReturn( outputEsperado ).when( service ).salvar( any( LembreteInputDTO.class ) );

        HttpHeaders headers = new HttpHeaders( );
        headers.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<LembreteInputDTO> request = new HttpEntity<>( input, headers );

        // 2. Act
        ResponseEntity<LembreteOutputDTO> response = restTemplate
                .postForEntity( "/lembrete", request, LembreteOutputDTO.class );

        LembreteOutputDTO responseBody = response.getBody();
        assertNotNull(responseBody);

        //3. Assert
        assertAll(
                ( ) -> assertThat( response.getStatusCode( ).value( ) ).isEqualTo( 200 ),
                ( ) -> assertEquals( MediaType.APPLICATION_JSON, response.getHeaders( ).getContentType( ) ),
                ( ) -> assertEquals( outputEsperado.titulo( ), responseBody.titulo( ) ),
                ( ) -> assertEquals( outputEsperado.descricao( ), responseBody.descricao( ) ),
                ( ) -> assertEquals( outputEsperado.status( ), responseBody.status( ) ),
                ( ) -> assertEquals( outputEsperado.categoria( ), responseBody.categoria( ) ),
                ( ) -> assertEquals( outputEsperado.recorrencia( ), responseBody.recorrencia( ) ),
                ( ) -> assertEquals( outputEsperado.datasEspecificas( ), responseBody.datasEspecificas( ) ),
                ( ) -> assertEquals( outputEsperado.metodoNotificacao( ), responseBody.metodoNotificacao( ) ) );
    }

    @Test
    @DisplayName("Testa retorno de salvar com campos errados")
    void testaSalvar_02()
    {  }
}
