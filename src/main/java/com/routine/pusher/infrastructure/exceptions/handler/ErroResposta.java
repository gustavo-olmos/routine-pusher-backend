package com.routine.pusher.infrastructure.exceptions.handler;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Corpo padronizado de resposta de erro da API. {@code camposInvalidos} só é preenchido em falhas de
 * validação (campo -> mensagem); nos demais casos vem nulo e é omitido do JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResposta(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> camposInvalidos
) {
    public static ErroResposta de( int status, String erro, String mensagem, String caminho )
    {
        return new ErroResposta( LocalDateTime.now( ), status, erro, mensagem, caminho, null );
    }

    public static ErroResposta deValidacao( int status, String erro, String mensagem,
                                            String caminho, Map<String, String> camposInvalidos )
    {
        return new ErroResposta( LocalDateTime.now( ), status, erro, mensagem, caminho, camposInvalidos );
    }
}
