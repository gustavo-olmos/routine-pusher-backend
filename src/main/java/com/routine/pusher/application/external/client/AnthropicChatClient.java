package com.routine.pusher.application.external.client;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.errors.AnthropicException;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.errors.RateLimitException;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.JsonOutputFormat;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.TextBlockParam;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapter da Anthropic: usa structured outputs — o servidor obriga o modelo a responder no formato
 * do schema. Schema, regras e contexto vêm do {@link ContratoLembreteIA}; aqui mora só o que é
 * específico deste provedor.
 *
 * <p>Ativo quando {@code ia.provedor=anthropic}. Requer crédito de API (console.anthropic.com), que
 * é carteira separada de qualquer assinatura do claude.ai.</p>
 */
@Service
@ConditionalOnProperty(name = "ia.provedor", havingValue = "anthropic")
public class AnthropicChatClient implements ChatClient<LembreteInputDTO>
{
    private static final Logger LOGGER = LoggerFactory.getLogger( AnthropicChatClient.class );

    /**
     * Baixo de propósito: a resposta é um JSON de algumas centenas de tokens, e o teto limita o
     * custo do pior caso — tokens de saída são os mais caros.
     */
    private static final long MAXIMO_TOKENS_RESPOSTA = 2048L;

    private final AnthropicClient client;
    private final String modelo;
    private final ObjectMapper objectMapper;
    private final ContratoLembreteIA contrato;
    private final JsonOutputFormat formatoDeSaida;

    public AnthropicChatClient( @Value("${anthropic.api-key:}") String apiKey,
                                @Value("${anthropic.model:claude-haiku-4-5}") String modelo,
                                ObjectMapper objectMapper,
                                ContratoLembreteIA contrato )
    {
        String chave = ( apiKey == null || apiKey.isBlank( ) ) ? "nao-configurada" : apiKey;
        this.client = AnthropicOkHttpClient.builder( ).apiKey( chave ).build( );
        this.modelo = modelo;
        this.objectMapper = objectMapper;
        this.contrato = contrato;
        this.formatoDeSaida = montarFormatoDeSaida( );
    }

    @Override
    public LembreteInputDTO montarLembrete( String frase, LocalDateTime agora, List<Categoria> categorias )
    {
        MessageCreateParams params = MessageCreateParams.builder( )
                .model( modelo )
                .maxTokens( MAXIMO_TOKENS_RESPOSTA )
                // cache_control no prompt de sistema: abaixo do prefixo mínimo do modelo ele
                // simplesmente não cacheia, sem erro; se o prompt crescer, o desconto liga sozinho.
                .systemOfTextBlockParams( List.of( TextBlockParam.builder( )
                        .text( contrato.promptDeSistema( ) )
                        .cacheControl( CacheControlEphemeral.builder( ).build( ) )
                        .build( ) ) )
                .outputConfig( OutputConfig.builder( ).format( formatoDeSaida ).build( ) )
                .addUserMessage( contrato.montarContexto( frase, agora, categorias ) )
                .build( );

        String json = extrairTexto( executar( params ) );

        try {
            return objectMapper.readValue( json, LembreteInputDTO.class );
        }
        catch ( Exception ex ) {
            LOGGER.error( "Structured output ilegível do modelo {}: {}", modelo, json, ex );
            throw new ConversaoException( ex.getMessage( ) );
        }
    }

    /**
     * Do mais específico ao mais genérico, porque a reação difere: limite de taxa é transitório
     * (tente já), os demais são indisponibilidade. Nenhuma mensagem do provedor vaza ao cliente —
     * elas descrevem a nossa conta, não o problema do visitante.
     */
    private Message executar( MessageCreateParams params )
    {
        try {
            return client.messages( ).create( params );
        }
        catch ( RateLimitException e ) {
            LOGGER.warn( "Limite de taxa da API de IA atingido", e );
            throw new ProcessoException( "Chat de lembrete",
                    "O serviço de IA está ocupado; tente novamente em alguns segundos" );
        }
        catch ( AnthropicServiceException e ) {
            LOGGER.error( "Erro da API de IA (status {})", e.statusCode( ), e );
            throw new ProcessoException( "Chat de lembrete",
                    "O serviço de IA não conseguiu processar a frase" );
        }
        catch ( AnthropicException e ) {
            LOGGER.error( "Falha de comunicação com a API de IA", e );
            throw new ProcessoException( "Chat de lembrete",
                    "O serviço de IA está indisponível no momento" );
        }
    }

    private String extrairTexto( Message resposta )
    {
        String texto = resposta.content( ).stream( )
                .flatMap( bloco -> bloco.text( ).stream( ) )
                .map( TextBlock::text )
                .collect( Collectors.joining( ) );

        if( texto.isBlank( ) )
            throw new ProcessoException( "Chat de lembrete", "O modelo não retornou conteúdo" );

        return texto;
    }

    /**
     * O dialeto da Anthropic aceita o schema canônico como está — inclusive
     * {@code additionalProperties: false}, que structured outputs usa para fechar o objeto.
     */
    private JsonOutputFormat montarFormatoDeSaida( )
    {
        Map<String, Object> campos =
                objectMapper.convertValue( contrato.schema( ), new TypeReference<>( ) { } );

        JsonOutputFormat.Schema.Builder builder = JsonOutputFormat.Schema.builder( );
        campos.forEach( ( nome, valor ) -> builder.putAdditionalProperty( nome, JsonValue.from( valor ) ) );

        return JsonOutputFormat.builder( ).schema( builder.build( ) ).build( );
    }
}
