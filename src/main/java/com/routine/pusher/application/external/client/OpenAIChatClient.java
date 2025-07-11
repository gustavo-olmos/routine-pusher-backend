package com.routine.pusher.application.external.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;
import com.openai.models.ChatModel;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.infrastructure.exceptions.ConversaoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAIChatClient implements ChatClient<LembreteInputDTO>
{
    private final String BANANA = "BANANA";

    private final ObjectMapper objectMapper = new ObjectMapper( );
    private final OpenAIClient client = OpenAIOkHttpClient.builder().apiKey(BANANA).build();

    private String prompt = """
            ATENÇÃO, Sua resposta deve ser somente o JSON solicitado abaixo.\s
            Utilizando os dados digitados pelo usuário, crie uma estrutura JSON de lembrete como esta:\s
        """;


    @Override
    public LembreteInputDTO buildLembreteChat( String frase ) throws JsonProcessingException,
            ConversaoException
    {
        prompt = prompt + objectMapper.writeValueAsString( LembreteInputDTO.class ) +
                "\n Dados de lembrete digitados pelo usuário: \n" + frase;

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder( )
                .addUserMessage( prompt )
                .model( ChatModel.O3_MINI )
                .build( );

        ChatCompletion completion = client.chat( ).completions( ).create( params );
        List<ChatCompletion.Choice> choices = completion.choices( );
        if( choices.isEmpty( ) ) return null;

        String content = choices.get( 0 ).message( ).content( ).orElse( null );
        try {
            return objectMapper.readValue( content, LembreteInputDTO.class );
        }
        catch ( Exception ex ) {
            throw new ConversaoException( ex.getMessage( ) );
        }
    }
}
