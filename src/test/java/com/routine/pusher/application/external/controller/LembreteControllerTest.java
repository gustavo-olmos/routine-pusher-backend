package com.routine.pusher.application.external.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routine.pusher.application.usecase.AtualizarDetalhesUseCase;
import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.application.usecase.ConcluirUseCase;
import com.routine.pusher.core.domain.lembrete.dto.LembreteDetalhesInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.example.LembreteExample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = LembreteController.class)
@AutoConfigureMockMvc(addFilters = false) // isola a camada web: segurança/CSRF são testados à parte
class LembreteControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    /** O do contexto, e não um {@code new}: o corpo tem {@code LocalTime}, que pede o módulo de datas. */
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CRUDUseCase<LembreteInputDTO, LembreteOutputDTO, UUID> useCase;

    @MockBean
    private ConcluirUseCase<UUID> concluirUseCase;

    @MockBean
    private AtualizarDetalhesUseCase<LembreteDetalhesInputDTO, LembreteOutputDTO, UUID> detalhesUseCase;


    @Test
    @DisplayName("criar responde 201, com Location para o lembrete e o corpo completo com a prévia")
    void salvar_responde201ComLocation( ) throws Exception
    {
        LembreteOutputDTO criado = LembreteExample.simplesOutput( );
        when( useCase.adicionar( any( LembreteInputDTO.class ) ) ).thenReturn( criado );

        mockMvc.perform( post( "/api/v1/lembrete" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsString( LembreteExample.simplesInput( ) ) ) )
                .andExpect( status( ).isCreated( ) )
                .andExpect( header( ).string( "Location", "http://localhost/api/v1/lembrete/" + criado.uuid( ) ) )
                .andExpect( jsonPath( "$.uuid" ).value( criado.uuid( ).toString( ) ) )
                .andExpect( jsonPath( "$.titulo" ).value( criado.titulo( ) ) )
                .andExpect( jsonPath( "$.proximasExecucoes.length()" ).value( 2 ) )
                .andExpect( jsonPath( "$.proximasExecucoes[0]" ).value( "2025-09-05T16:20:00" ) );
    }

    @Test
    @DisplayName("o Location da criação responde: GET por uuid devolve o lembrete com a prévia")
    void buscar_porUuid( ) throws Exception
    {
        LembreteOutputDTO lembrete = LembreteExample.simplesOutput( );
        when( useCase.buscarPeloId( lembrete.uuid( ) ) ).thenReturn( lembrete );

        mockMvc.perform( get( "/api/v1/lembrete/{uuid}", lembrete.uuid( ) ) )
                .andExpect( status( ).isOk( ) )
                .andExpect( jsonPath( "$.uuid" ).value( lembrete.uuid( ).toString( ) ) )
                .andExpect( jsonPath( "$.proximasExecucoes.length()" ).value( 2 ) );
    }
}
