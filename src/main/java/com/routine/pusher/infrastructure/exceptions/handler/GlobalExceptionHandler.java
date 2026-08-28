package com.routine.pusher.infrastructure.exceptions.handler;

import com.routine.pusher.infrastructure.exceptions.ConversaoException;
import com.routine.pusher.infrastructure.exceptions.ExclusaoException;
import com.routine.pusher.infrastructure.exceptions.LimiteDeUsoException;
import com.routine.pusher.infrastructure.exceptions.ProcessoException;
import com.routine.pusher.infrastructure.exceptions.SortingException;
import com.routine.pusher.infrastructure.exceptions.StrategyException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento centralizado de exceções da API: converte cada falha num {@link ErroResposta} JSON com o
 * status HTTP adequado, evitando vazar stack trace ao cliente. Recursos não encontrados -> 404,
 * validação de entrada -> 400, conflito com o estado já persistido -> 409, falhas de regra/processo
 * do domínio -> 422, e um fallback 500.
 *
 * <p>Estende {@link ResponseEntityExceptionHandler} de propósito: ele já mapeia os erros próprios do
 * Spring MVC (rota inexistente, JSON malformado, parâmetro obrigatório ausente, verbo não suportado,
 * id de tipo errado) para os status corretos. Sem essa herança, o {@code @ExceptionHandler} de
 * {@link Exception} abaixo capturava todos eles e devolvia <b>500 para erro de cliente</b>.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger( GlobalExceptionHandler.class );

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado( EntityNotFoundException ex, HttpServletRequest req )
    {
        return construir( HttpStatus.NOT_FOUND, ex.getMessage( ), req.getRequestURI( ) );
    }

    /**
     * Restrição do banco violada — tipicamente coluna {@code unique} repetida, ou {@code not null}
     * que nenhuma anotação de validação cobriu. É conflito com o estado já persistido, não erro de
     * servidor. A causa real vai para o log, e não para a resposta, porque a mensagem do driver expõe
     * nomes de tabela, coluna e o valor que colidiu.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> tratarConflitoDeDados( DataIntegrityViolationException ex,
                                                               HttpServletRequest req )
    {
        LOGGER.warn( "Violação de integridade ao processar {}", req.getRequestURI( ), ex );

        return construir( HttpStatus.CONFLICT,
                "A operação conflita com dados já existentes: verifique os campos únicos e obrigatórios",
                req.getRequestURI( ) );
    }

    @ExceptionHandler(SortingException.class)
    public ResponseEntity<ErroResposta> tratarOrdenacao( SortingException ex, HttpServletRequest req )
    {
        return construir( HttpStatus.BAD_REQUEST, ex.getMessage( ), req.getRequestURI( ) );
    }

    @ExceptionHandler({ StrategyException.class, ProcessoException.class,
            ExclusaoException.class, ConversaoException.class })
    public ResponseEntity<ErroResposta> tratarRegraDeNegocio( Exception ex, HttpServletRequest req )
    {
        return construir( HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage( ), req.getRequestURI( ) );
    }

    /**
     * 429, e não 422: o payload não tem nada errado a corrigir — o que falta é espaço na cota da
     * sessão. O status distinto deixa o front tratar o caso do próprio jeito (avisar e oferecer a
     * limpeza), sem inspecionar texto de mensagem.
     */
    @ExceptionHandler(LimiteDeUsoException.class)
    public ResponseEntity<ErroResposta> tratarLimiteDeUso( LimiteDeUsoException ex, HttpServletRequest req )
    {
        return construir( HttpStatus.TOO_MANY_REQUESTS, ex.getMessage( ), req.getRequestURI( ) );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarInesperado( Exception ex, HttpServletRequest req )
    {
        LOGGER.error( "Erro inesperado ao processar {}", req.getRequestURI( ), ex );
        return construir( HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado", req.getRequestURI( ) );
    }

    /**
     * Sobrescrito, e não declarado como {@code @ExceptionHandler} próprio, porque a superclasse já
     * trata este tipo — duas declarações para a mesma exceção quebrariam a aplicação na subida por
     * ambiguidade. É o único caso que enriquece a resposta com o mapa campo -> mensagem.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid( MethodArgumentNotValidException ex,
                                                                   HttpHeaders headers,
                                                                   HttpStatusCode status,
                                                                   WebRequest request )
    {
        Map<String, String> campos = new LinkedHashMap<>( );
        for( FieldError erro : ex.getBindingResult( ).getFieldErrors( ) )
            campos.putIfAbsent( erro.getField( ), erro.getDefaultMessage( ) );

        ErroResposta corpo = ErroResposta.deValidacao(
                HttpStatus.BAD_REQUEST.value( ),
                HttpStatus.BAD_REQUEST.getReasonPhrase( ),
                "Um ou mais campos são inválidos",
                caminhoDe( request ),
                campos );

        return ResponseEntity.badRequest( ).body( corpo );
    }

    /**
     * Ponto único por onde passam todas as respostas montadas pela superclasse. Reescreve o corpo
     * padrão ({@link ProblemDetail}) no formato {@link ErroResposta}, para a API ter uma só forma de
     * erro. O texto vem do {@code detail} que o próprio Spring redigiu para o cliente — usar
     * {@code ex.getMessage()} aqui exporia assinatura de método e tipos internos.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal( Exception ex, Object corpoPadrao,
                                                              HttpHeaders headers, HttpStatusCode status,
                                                              WebRequest request )
    {
        HttpStatus httpStatus = HttpStatus.valueOf( status.value( ) );

        // A superclasse entrega corpo nulo na maioria dos casos e só materializa o ProblemDetail
        // dentro deste método — que foi sobrescrito. Sem repetir o passo, perderíamos o texto útil
        // ("Required parameter 'sortInfo' is not present") e sobraria só o nome do status.
        if( corpoPadrao == null && ex instanceof ErrorResponse erro )
            corpoPadrao = erro.updateAndGetBody( getMessageSource( ), LocaleContextHolder.getLocale( ) );

        String mensagem = corpoPadrao instanceof ProblemDetail detalhe && detalhe.getDetail( ) != null
                ? detalhe.getDetail( )
                : httpStatus.getReasonPhrase( );

        ErroResposta corpo = ErroResposta.de(
                httpStatus.value( ), httpStatus.getReasonPhrase( ), mensagem, caminhoDe( request ) );

        return ResponseEntity.status( status ).headers( headers ).body( corpo );
    }

    private ResponseEntity<ErroResposta> construir( HttpStatus status, String mensagem, String caminho )
    {
        ErroResposta corpo = ErroResposta.de( status.value( ), status.getReasonPhrase( ), mensagem, caminho );

        return ResponseEntity.status( status ).body( corpo );
    }

    private String caminhoDe( WebRequest request )
    {
        return request instanceof ServletWebRequest servlet
                ? servlet.getRequest( ).getRequestURI( )
                : request.getDescription( false );
    }
}
