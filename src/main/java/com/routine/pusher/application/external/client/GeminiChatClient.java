package com.routine.pusher.application.external.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.genai.Client;
import com.google.genai.errors.ApiException;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.Schema;
import com.google.genai.types.ThinkingConfig;
import com.google.genai.types.ThinkingLevel;
import com.routine.pusher.core.domain.categoria.Categoria;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.infrastructure.exceptions.ConversaoException;
import com.routine.pusher.infrastructure.exceptions.ProcessoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Adapter do Gemini, provedor padrão do demo: o free tier do Google AI Studio não pede cartão, o
 * que permite validar o funcionamento real sem comprar crédito. Schema, regras e contexto vêm do
 * {@link ContratoLembreteIA} — aqui mora só o que é específico deste provedor.
 *
 * <p>Ativo quando {@code ia.provedor=gemini} (o padrão). O teto do free tier é <b>diário</b>: ao
 * estourar, o provedor devolve 429 e o visitante recebe a mesma mensagem de indisponibilidade de
 * qualquer outra falha — degradação, não quebra.</p>
 */
@Service
@ConditionalOnProperty(name = "ia.provedor", havingValue = "gemini", matchIfMissing = true)
public class GeminiChatClient implements ChatClient<LembreteInputDTO>
{
    private static final Logger LOGGER = LoggerFactory.getLogger( GeminiChatClient.class );

    /**
     * Nos modelos Gemini 3.x o raciocínio consome desta mesma conta, então o teto precisa caber
     * raciocínio + resposta. O JSON de um lembrete tem algumas centenas de tokens; a folga aqui é
     * para o raciocínio não empurrar a resposta para fora e devolver JSON cortado no meio.
     */
    private static final int MAXIMO_TOKENS_RESPOSTA = 4096;

    /**
     * Zero parece a escolha óbvia para extração estruturada — e é justamente o que trava o modelo em
     * laço de repetição: decodificação gulosa reescolhe o mesmo token para sempre. Um valor baixo,
     * porém não nulo, mantém a resposta estável sem o laço. (Custou um título de 8.749 caracteres
     * para descobrir.)
     */
    private static final float TEMPERATURA = 0.2f;

    private static final String TIPO_JSON = "application/json";

    private final Client client;
    private final String modelo;
    private final ObjectMapper objectMapper;
    private final ContratoLembreteIA contrato;
    private final GenerateContentConfig config;

    public GeminiChatClient( @Value("${gemini.api-key:}") String apiKey,
                             @Value("${gemini.model:gemini-3.5-flash-lite}") String modelo,
                             ObjectMapper objectMapper,
                             ContratoLembreteIA contrato )
    {
        String chave = ( apiKey == null || apiKey.isBlank( ) ) ? "nao-configurada" : apiKey;
        this.client = Client.builder( ).apiKey( chave ).build( );
        this.modelo = modelo;
        this.objectMapper = objectMapper;
        this.contrato = contrato;
        this.config = montarConfig( );
    }

    @Override
    public LembreteInputDTO montarLembrete( String frase, LocalDateTime agora, List<Categoria> categorias )
    {
        String json = extrairTexto( executar( contrato.montarContexto( frase, agora, categorias ) ) );

        try {
            return objectMapper.readValue( json, LembreteInputDTO.class );
        }
        catch ( Exception ex ) {
            LOGGER.error( "Resposta ilegível do modelo {}: {}", modelo, json, ex );
            throw new ConversaoException( ex.getMessage( ) );
        }
    }

    /**
     * 429 é o caso esperado do free tier (cota diária), e é transitório: a mensagem convida a tentar
     * de novo em vez de sugerir que o site quebrou. Nenhum texto do provedor vaza ao visitante —
     * ele descreve a nossa conta, não o problema de quem digitou a frase.
     */
    private GenerateContentResponse executar( String contexto )
    {
        try {
            return client.models.generateContent( modelo, contexto, config );
        }
        catch ( ApiException e ) {
            if( e.code( ) == 429 ) {
                LOGGER.warn( "Cota do provedor de IA esgotada (429)", e );
                throw new ProcessoException( "Chat de lembrete",
                        "O serviço de IA atingiu o limite de uso; tente novamente mais tarde" );
            }

            LOGGER.error( "Erro da API de IA (status {})", e.code( ), e );
            throw new ProcessoException( "Chat de lembrete",
                    "O serviço de IA não conseguiu processar a frase" );
        }
        catch ( RuntimeException e ) {
            LOGGER.error( "Falha de comunicação com a API de IA", e );
            throw new ProcessoException( "Chat de lembrete",
                    "O serviço de IA está indisponível no momento" );
        }
    }

    private String extrairTexto( GenerateContentResponse resposta )
    {
        String texto = resposta.text( );

        // Resposta vazia aqui costuma ser corte por filtro de conteúdo ou por maxOutputTokens — nos
        // dois casos não há JSON para ler, e tratar como indisponibilidade é mais honesto que
        // devolver erro de conversão sobre uma string vazia.
        if( texto == null || texto.isBlank( ) )
            throw new ProcessoException( "Chat de lembrete", "O modelo não retornou conteúdo" );

        return texto;
    }

    private GenerateContentConfig montarConfig( )
    {
        return GenerateContentConfig.builder( )
                .systemInstruction( Content.fromParts( Part.fromText( contrato.promptDeSistema( ) ) ) )
                .responseMimeType( TIPO_JSON )
                .responseSchema( schemaDoProvedor( ) )
                .maxOutputTokens( MAXIMO_TOKENS_RESPOSTA )
                .temperature( TEMPERATURA )
                // Traduzir uma frase em campos não é problema de raciocínio longo, e no free tier o
                // raciocínio disputa o mesmo teto de tokens da resposta: gastar ali é gastar duas
                // vezes, em cota e em espaço para o JSON.
                .thinkingConfig( ThinkingConfig.builder( )
                        .thinkingLevel( new ThinkingLevel( ThinkingLevel.Known.LOW ) )
                        .build( ) )
                .build( );
    }

    /**
     * O schema do Gemini é um subset do OpenAPI, que não conhece {@code additionalProperties} — o
     * canônico traz esse campo porque a Anthropic o usa para fechar o objeto. Podar aqui, e não no
     * contrato, mantém a quirk do dialeto junto do provedor que a tem.
     */
    private Schema schemaDoProvedor( )
    {
        ObjectNode schema = contrato.schema( );
        podarAdditionalProperties( schema );

        try {
            return Schema.fromJson( objectMapper.writeValueAsString( schema ) );
        }
        catch ( Exception e ) {
            throw new IllegalStateException( "Schema de lembrete incompatível com o dialeto do Gemini", e );
        }
    }

    private void podarAdditionalProperties( JsonNode no )
    {
        if( no.isObject( ) ) {
            ( (ObjectNode) no ).remove( "additionalProperties" );
            for( Iterator<Map.Entry<String, JsonNode>> campos = no.fields( ); campos.hasNext( ); )
                podarAdditionalProperties( campos.next( ).getValue( ) );
        }
        else if( no.isArray( ) ) {
            no.forEach( this::podarAdditionalProperties );
        }
    }
}
