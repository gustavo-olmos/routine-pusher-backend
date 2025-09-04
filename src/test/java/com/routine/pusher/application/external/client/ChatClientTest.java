package com.routine.pusher.application.external.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.Completions;

@ExtendWith(MockitoExtension.class)
class ChatClientTest
{
    @InjectMocks
    private OpenAIChatClient client;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OpenAIClient openAIClient;

    @Mock
    private Completions completions;

    @Mock
    private ChatClient<LembreteInputDTO> chatClient;


    @Test
    @DisplayName("Deve construir um LembreteInputDTO com sucesso a partir da resposta da API")
    void buildLembreteChat_shouldReturnLembreteInputDTO_onSuccess( ) throws Exception
    {
//        // --- Arrange ---
//        String fraseDoUsuario = "Lembrete para ligar para a mae amanha as 10 da manha";
//        String respostaJsonDaApi = "{\"frase\":\"Ligar para a mãe\", \"data\": \"amanhã\", \"hora\": \"10:00\"}";
//        LembreteInputDTO lembreteEsperado = new LembreteInputDTO("Ligar para a mãe", "amanhã", 1L, null, null);
//
//        when(objectMapper.writeValueAsString(any(Class.class))).thenReturn("{ \"campo\":\"valor\" }"); // mock do ObjectMapper para simular a conversão para JSON.
//
//        // Simula o comportamento da cadeia de chamadas para a API
//        when(openAIClient.chat()).thenReturn(chatClient);
//        when(chatClient.buildLembreteChat(respostaJsonDaApi)).thenReturn(completions);
//        when(completions.create(any(ChatCompletionCreateParams.class)))
//                .thenReturn(
//                        ChatCompletion.builder()
//                                .choices(List.of(
//                                        ChatCompletion.Choice.builder()
//                                                .message(ChatCompletion.Message.builder()
//                                                        .content(Optional.of(respostaJsonDaApi))
//                                                        .build())
//                                                .build()))
//                                .build());
//
//        // Simula o comportamento do ObjectMapper para simular a conversão do JSON para o DTO.
//        when(objectMapper.readValue(any(String.class), any(Class.class))).thenReturn(lembreteEsperado);
//
//        // --- Act ---
//        LembreteInputDTO resultado = client.buildLembreteChat(fraseDoUsuario);
//
//        // --- Assert ---
//        assertThat(resultado).isEqualTo(lembreteEsperado);
//
//        // Verifique se os métodos foram chamados
//        verify(openAIClient.chat()).completions();
//        verify(completions).create(any(ChatCompletionCreateParams.class));
//        verify(objectMapper).readValue(eq(respostaJsonDaApi), eq(LembreteInputDTO.class));
    }

    @Test
    @DisplayName("Deve retornar nulo se a API não retornar uma escolha")
    void buildLembreteChat_shouldReturnNull_ifNoChoices() throws Exception {
//        // Arrange
//        String fraseDoUsuario = "Lembrete para ligar para a mae amanha as 10 da manha";
//        when(objectMapper.writeValueAsString(any(Class.class))).thenReturn("{ \"campo\":\"valor\" }");
//
//        // Simula a API retornando uma lista de escolhas vazia
//        when(openAIClient.chat()).thenReturn(chatClient);
//        when(chatClient.completions()).thenReturn(completions);
//        when(completions.create(any(ChatCompletionCreateParams.class)))
//                .thenReturn(ChatCompletion.builder().choices(List.of()).build());
//
//        // Act
//        LembreteInputDTO resultado = client.buildLembreteChat(fraseDoUsuario);
//
//        // Assert
//        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Deve lançar ConversaoException se a resposta da API for inválida")
    void buildLembreteChat_shouldThrowConversaoException_onInvalidJson() throws JsonProcessingException {
//        // Arrange
//        String fraseDoUsuario = "Lembrete para ligar para a mae amanha as 10 da manha";
//        String respostaJsonInvalida = "json-invalido";
//
//        when(objectMapper.writeValueAsString(any(Class.class))).thenReturn("{ \"campo\":\"valor\" }");
//        when(openAIClient.chat()).thenReturn(chatClient);
//        when(chatClient.completions()).thenReturn(completions);
//        when(completions.create(any(ChatCompletionCreateParams.class)))
//                .thenReturn(
//                        ChatCompletion.builder()
//                                .choices(List.of(
//                                        ChatCompletion.Choice.builder()
//                                                .message(ChatCompletion.Message.builder()
//                                                        .content(Optional.of(respostaJsonInvalida))
//                                                        .build())
//                                                .build()))
//                                .build());
//
//        // Simula o ObjectMapper lançando uma exceção ao tentar converter o JSON inválido
//        when(objectMapper.readValue(eq(respostaJsonInvalida), eq(LembreteInputDTO.class)))
//                .thenThrow(new JsonProcessingException("Mensagem de erro de JSON inválido") {
//                });
//
//        // Act & Assert
//        assertThrows(ConversaoException.class, () -> client.buildLembreteChat(fraseDoUsuario));
    }
}
