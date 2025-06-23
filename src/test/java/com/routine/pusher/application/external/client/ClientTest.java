package com.routine.pusher.application.external.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.routine.pusher.core.example.input.LembreteInputDTOExample;
import com.routine.pusher.core.example.output.LembreteOutputDTOExample;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.infrastructure.exceptions.ConversaoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ClientTest
{
    @Mock
    OpenAIChatClient client;

    @Test
    @DisplayName("Processar: Testa XXX de YYY com ZZZ")
    void testProcessar01( ) throws ConversaoException, JsonProcessingException
    {
        LembreteInputDTO input   = LembreteInputDTOExample.simples( );
        LembreteOutputDTO retorno = LembreteOutputDTOExample.simples( );

        doReturn( retorno ).when( client ).buildLembreteChat( anyString( ) );

        LembreteInputDTO output   = new OpenAIChatClientImpl( ).buildLembreteChat( String.valueOf( input ) );
        LembreteOutputDTO esperado = LembreteOutputDTOExample.simples( );

        assertAll(
                ( ) -> assertEquals( esperado.titulo( ), output.titulo( ) ),
                ( ) -> assertEquals( esperado.descricao( ), output.descricao( ) ),
                ( ) -> assertEquals( esperado.categoria( ).id( ), output.categoriaId( ) ) );
    }
}
