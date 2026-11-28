package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.usecase.InterpretacaoUseCase;
import com.routine.pusher.core.domain.lembrete.dto.InterpretacaoLembreteOutputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteChatInputDTO;
import com.routine.pusher.example.LembreteExample;
import com.routine.pusher.infrastructure.exceptions.ConversaoException;
import com.routine.pusher.infrastructure.exceptions.IaIndisponivelException;
import com.routine.pusher.infrastructure.exceptions.LimiteDeUsoException;
import com.routine.pusher.infrastructure.exceptions.handler.GlobalExceptionHandler;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * O contrato HTTP da interpretação: o corpo que a tela de revisão lê, e os status que ela precisa
 * distinguir. O advice entra no contexto porque 429 e 422 são justamente o mapeamento dele.
 */
@WebMvcTest
@ContextConfiguration(classes = { ChatController.class, GlobalExceptionHandler.class })
@AutoConfigureMockMvc(addFilters = false)
class ChatControllerTest
{
    private static final String FRASE = "{\"frase\":\"conferir o extrato toda segunda\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InterpretacaoUseCase<InterpretacaoLembreteOutputDTO> interpretacaoUseCase;


    private ResultActions interpretar( ) throws Exception
    {
        return mockMvc.perform( post( "/api/v1/chat/interpretacao" )
                .contentType( MediaType.APPLICATION_JSON )
                .content( FRASE ) );
    }

    @Test
    @DisplayName("interpretação responde 200 com o rascunho no formato de entrada e a prévia")
    void interpretacao_devolveRascunhoEPrevia( ) throws Exception
    {
        when( interpretacaoUseCase.interpretarLembrete( any( LembreteChatInputDTO.class ) ) )
                .thenReturn( new InterpretacaoLembreteOutputDTO( LembreteExample.simplesInput( ),
                        List.of( LocalDateTime.of( 2026, 12, 7, 9, 0 ) ) ) );

        interpretar( )
                .andExpect( status( ).isOk( ) )
                .andExpect( jsonPath( "$.lembrete.titulo" ).value( "TESTE" ) )
                .andExpect( jsonPath( "$.lembrete.categoriaId" ).value( 1 ) )
                .andExpect( jsonPath( "$.lembrete.notificacao.horario" ).value( "12:15" ) )
                .andExpect( jsonPath( "$.lembrete.uuid" ).doesNotExist( ) )
                .andExpect( jsonPath( "$.proximasExecucoes[0]" ).value( "2026-12-07T09:00:00" ) );
    }

    @Test
    @DisplayName("cota de IA esgotada vira 429")
    void interpretacao_semCota_429( ) throws Exception
    {
        when( interpretacaoUseCase.interpretarLembrete( any( ) ) )
                .thenThrow( new LimiteDeUsoException( "Limite de 10 chamadas de IA por sessão atingido" ) );

        interpretar( )
                .andExpect( status( ).isTooManyRequests( ) )
                .andExpect( jsonPath( "$.mensagem" ).value( containsString( "chamadas de IA" ) ) );
    }

    @Test
    @DisplayName("IA fora ou freada vira 503 com Retry-After, não erro de regra")
    void interpretacao_iaIndisponivel_503( ) throws Exception
    {
        when( interpretacaoUseCase.interpretarLembrete( any( ) ) )
                .thenThrow( new IaIndisponivelException( "Muita gente testando a IA agora",
                        Duration.ofMillis( 4200 ), false ) );

        interpretar( )
                .andExpect( status( ).isServiceUnavailable( ) )
                .andExpect( header( ).string( "Retry-After", "5" ) )
                .andExpect( jsonPath( "$.mensagem" ).value( containsString( "Muita gente" ) ) );
    }

    @Test
    @DisplayName("rascunho inválido da IA vira 422 apontando o campo")
    void interpretacao_rascunhoInvalido_422( ) throws Exception
    {
        when( interpretacaoUseCase.interpretarLembrete( any( ) ) )
                .thenThrow( new ConversaoException( "a IA montou um lembrete inválido "
                        + "(titulo: O título do lembrete é obrigatório)" ) );

        interpretar( )
                .andExpect( status( ).isUnprocessableEntity( ) )
                .andExpect( jsonPath( "$.mensagem" ).value( containsString( "titulo" ) ) );
    }

    @Test
    @DisplayName("frase em branco para na validação da entrada, antes de qualquer cota")
    void interpretacao_fraseEmBranco_400( ) throws Exception
    {
        mockMvc.perform( post( "/api/v1/chat/interpretacao" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( "{\"frase\":\" \"}" ) )
                .andExpect( status( ).isBadRequest( ) )
                .andExpect( jsonPath( "$.camposInvalidos.frase" ).exists( ) );
    }

    @Test
    @DisplayName("o endpoint que salvava sem revisão saiu: o modelo não tem rota para persistir")
    void lembreteAntigo_naoExisteMais( ) throws Exception
    {
        mockMvc.perform( post( "/api/v1/chat/lembrete" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( FRASE ) )
                // 404 ou 405, conforme o handler de recursos estáticos responda ao POST: o que
                // importa é que nenhuma rota de chat aceite mais o pedido.
                .andExpect( status( ).is4xxClientError( ) );
    }
}
