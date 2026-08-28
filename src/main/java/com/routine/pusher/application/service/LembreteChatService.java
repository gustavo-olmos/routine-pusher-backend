package com.routine.pusher.application.service;

import com.routine.pusher.application.external.client.ChatClient;
import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.application.usecase.ChatUseCase;
import com.routine.pusher.core.domain.categoria.port.CategoriaQueryPort;
import com.routine.pusher.core.domain.lembrete.dto.LembreteChatInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.sessao.SessaoAnonima;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaRepository;
import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
import com.routine.pusher.infrastructure.exceptions.ConversaoException;
import com.routine.pusher.infrastructure.exceptions.LimiteDeUsoException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Orquestra a criação de lembrete por linguagem natural. O provedor de IA fica atrás do
 * {@link ChatClient}; aqui moram as regras que não podem depender de modelo nenhum: a cota por
 * sessão (custo), o contexto que o modelo recebe e a validação do que ele devolve.
 */
@Service
@AllArgsConstructor
public class LembreteChatService implements ChatUseCase<LembreteOutputDTO>
{
    private static final Logger LOGGER = LoggerFactory.getLogger( LembreteChatService.class );

    private final ChatClient<LembreteInputDTO> client;
    private final CRUDUseCase<LembreteInputDTO, LembreteOutputDTO, UUID> useCase;
    private final CategoriaQueryPort categoriaQueryPort;
    private final SessaoAtualPort sessaoAtual;
    private final SessaoAnonimaRepository sessaoRepository;
    private final Validator validator;


    @Override
    public LembreteOutputDTO criarLembreteViaChat( LembreteChatInputDTO entrada )
    {
        LOGGER.debug( "Montando lembrete via chat" );

        consumirCotaDeIa( );

        LocalDateTime agora = entrada.agora( ) != null ? entrada.agora( ) : LocalDateTime.now( );

        LembreteInputDTO lembrete =
                client.montarLembrete( entrada.frase( ), agora, categoriaQueryPort.listar( ) );

        validar( lembrete );

        try {
            return useCase.adicionar( lembrete );
        }
        catch ( RuntimeException e ) {
            // Sem isto, uma frase que o modelo interpreta mal vira só a mensagem de erro do
            // domínio, e não há como saber o que ele montou — no demo eu não estarei olhando.
            LOGGER.warn( "Lembrete montado pela IA recusado pelo domínio ({}): {}",
                    e.getMessage( ), lembrete );
            throw e;
        }
    }

    /**
     * A cota é consumida <b>antes</b> da chamada, atomicamente: é o gasto que ela protege, então a
     * vaga vale a partir do momento em que decidimos chamar o modelo — não do momento em que ele
     * responde. Ver {@code SessaoAnonimaRepository#consumirChamadaIa}.
     */
    private void consumirCotaDeIa( )
    {
        int consumidas = sessaoRepository.consumirChamadaIa(
                sessaoAtual.uuid( ), SessaoAnonima.LIMITE_CHAMADAS_IA );

        if( consumidas == 0 )
            throw new LimiteDeUsoException( "Limite de " + SessaoAnonima.LIMITE_CHAMADAS_IA
                    + " chamadas de IA por sessão atingido: crie o lembrete pelo formulário" );
    }

    /**
     * A resposta do modelo entra pela mesma régua do controller: o Bean Validation do próprio DTO.
     * Structured output garante a <i>forma</i> do JSON; obrigatoriedade e faixa de valores são
     * contrato nosso, e contrato se confere na porta — venha o dado de um humano ou de um modelo.
     */
    private void validar( LembreteInputDTO lembrete )
    {
        Set<ConstraintViolation<LembreteInputDTO>> violacoes = validator.validate( lembrete );
        if( violacoes.isEmpty( ) ) return;

        String detalhes = violacoes.stream( )
                .map( v -> v.getPropertyPath( ) + ": " + v.getMessage( ) )
                .sorted( )
                .collect( Collectors.joining( "; " ) );

        LOGGER.warn( "Lembrete montado pela IA reprovado na validação: {}", detalhes );

        throw new ConversaoException( "a IA montou um lembrete inválido (" + detalhes
                + "); tente reformular a frase com mais detalhes" );
    }
}
