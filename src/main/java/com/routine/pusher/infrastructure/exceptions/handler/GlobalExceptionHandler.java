package com.routine.pusher.infrastructure.exceptions.handler;

import com.routine.pusher.infrastructure.exceptions.ConversaoException;
import com.routine.pusher.infrastructure.exceptions.ExclusaoException;
import com.routine.pusher.infrastructure.exceptions.ProcessoException;
import com.routine.pusher.infrastructure.exceptions.SortingException;
import com.routine.pusher.infrastructure.exceptions.StrategyException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento centralizado de exceções da API: converte cada falha num {@link ErroResposta} JSON com o
 * status HTTP adequado, evitando vazar stack trace ao cliente. Recursos não encontrados -> 404,
 * validação de entrada -> 400, falhas de regra/processo do domínio -> 422, e um fallback 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger( GlobalExceptionHandler.class );

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado( EntityNotFoundException ex, HttpServletRequest req )
    {
        return construir( HttpStatus.NOT_FOUND, ex.getMessage( ), req );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao( MethodArgumentNotValidException ex, HttpServletRequest req )
    {
        Map<String, String> campos = new LinkedHashMap<>( );
        for( FieldError erro : ex.getBindingResult( ).getFieldErrors( ) )
            campos.putIfAbsent( erro.getField( ), erro.getDefaultMessage( ) );

        ErroResposta corpo = ErroResposta.deValidacao(
                HttpStatus.BAD_REQUEST.value( ),
                HttpStatus.BAD_REQUEST.getReasonPhrase( ),
                "Um ou mais campos são inválidos",
                req.getRequestURI( ),
                campos );

        return ResponseEntity.badRequest( ).body( corpo );
    }

    @ExceptionHandler(SortingException.class)
    public ResponseEntity<ErroResposta> tratarOrdenacao( SortingException ex, HttpServletRequest req )
    {
        return construir( HttpStatus.BAD_REQUEST, ex.getMessage( ), req );
    }

    @ExceptionHandler({ StrategyException.class, ProcessoException.class,
            ExclusaoException.class, ConversaoException.class })
    public ResponseEntity<ErroResposta> tratarRegraDeNegocio( Exception ex, HttpServletRequest req )
    {
        return construir( HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage( ), req );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarInesperado( Exception ex, HttpServletRequest req )
    {
        LOGGER.error( "Erro inesperado ao processar {}", req.getRequestURI( ), ex );
        return construir( HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado", req );
    }

    private ResponseEntity<ErroResposta> construir( HttpStatus status, String mensagem, HttpServletRequest req )
    {
        ErroResposta corpo = ErroResposta.de(
                status.value( ), status.getReasonPhrase( ), mensagem, req.getRequestURI( ) );

        return ResponseEntity.status( status ).body( corpo );
    }
}
