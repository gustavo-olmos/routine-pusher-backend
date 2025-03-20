package com.routine.pusher.application.interfaces.client.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;
import com.openai.models.ChatModel;
import com.routine.pusher.application.interfaces.client.OpenAIChatClient;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.infrastructure.exceptions.ConversaoLembreteException;
import org.springframework.stereotype.Component;

@Component
public class OpenAIChatClientImpl implements OpenAIChatClient
{
    private final ObjectMapper objectMapper = new ObjectMapper( );
    private String prompt = """
            ATENÇÃO, Sua resposta deve ser somente o JSON solicitado abaixo.\s
            Utilizando os dados digitados pelo usuário, crie uma estrutura JSON de lembrete como esta:\s
        """;

    private final OpenAIClient client;

    public OpenAIChatClientImpl( OpenAIClient client )
    {
        this.client = OpenAIOkHttpClient.fromEnv();
    }

    @Override
    public LembreteOutputDTO buildLembreteChat( String frase ) throws JsonProcessingException,
            ConversaoLembreteException
    {
        prompt = prompt + objectMapper.writeValueAsString( LembreteOutputDTO.class ) +
                "\n Dados de lembrete digitados pelo usuário: \n" + frase;

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder( )
                .addUserMessage( prompt )
                .model( ChatModel.O3_MINI )
                .build( );

        ChatCompletion completion = client.chat( ).completions( ).create( params );
        var choices = completion.choices( );
        if ( choices.isEmpty( ) ) { return null; }

        var content = choices.get( 0 ).message( ).content( ).orElse( null );

        try {
            return objectMapper.readValue( content, LembreteOutputDTO.class );
        }
        catch ( Exception ex ) {
            throw new ConversaoLembreteException( ex.getMessage( ) );
        }
    }
}
