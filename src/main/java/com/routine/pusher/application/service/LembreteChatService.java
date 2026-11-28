package com.routine.pusher.application.service;

import com.routine.pusher.application.external.client.ChatClient;
import com.routine.pusher.application.usecase.InterpretacaoUseCase;
import com.routine.pusher.application.usecase.SimulacaoUseCase;
import com.routine.pusher.core.domain.categoria.port.CategoriaQueryPort;
import com.routine.pusher.core.domain.lembrete.dto.InterpretacaoLembreteOutputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteChatInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Orquestra o lembrete por linguagem natural. O provedor de IA fica atrás do {@link ChatClient};
 * aqui moram as regras que não podem depender de modelo nenhum: a cota por sessão (custo), o
 * contexto que o modelo recebe e a validação do que ele devolve.
 *
 * <p>O fluxo é interpretar, revisar e criar: {@link #interpretarLembrete} devolve um rascunho e
 * não salva nada, e a confirmação é um {@code POST /api/v1/lembrete} comum, que não gasta cota de
 * IA. O modelo não tem caminho para persistir: este serviço nem conhece o caso de uso de criação.</p>
 */
@Service
@AllArgsConstructor
public class LembreteChatService implements InterpretacaoUseCase<InterpretacaoLembreteOutputDTO>
{
    private static final Logger LOGGER = LoggerFactory.getLogger( LembreteChatService.class );

    private final ChatClient<LembreteInputDTO> client;
    private final SimulacaoUseCase<LembreteInputDTO, List<LocalDateTime>> simulacao;
    private final CategoriaQueryPort categoriaQueryPort;
    private final SessaoAtualPort sessaoAtual;
    private final SessaoAnonimaRepository sessaoRepository;
    private final Validator validator;


    /**
     * A ordem das travas segue o custo: primeiro a vaga de lembrete, que é uma contagem no banco;
     * depois a cota de IA, que é o recurso caro. Sem vaga, o rascunho não poderia ser confirmado,
     * e gastar uma chamada de modelo nele seria jogar cota fora.
     */
    @Override
    public InterpretacaoLembreteOutputDTO interpretarLembrete( LembreteChatInputDTO entrada )
    {
        LOGGER.debug( "Interpretando lembrete via chat" );

        simulacao.validarVagaNaSessao( );

        LembreteInputDTO lembrete = montarPeloModelo( entrada );

        try {
            return new InterpretacaoLembreteOutputDTO( lembrete, simulacao.simular( lembrete ) );
        }
        catch ( RuntimeException e ) {
            registrarRecusa( e, lembrete );
            throw e;
        }
    }

    private LembreteInputDTO montarPeloModelo( LembreteChatInputDTO entrada )
    {
        consumirCotaDeIa( );

        LocalDateTime agora = entrada.agora( ) != null ? entrada.agora( ) : LocalDateTime.now( );

        LembreteInputDTO lembrete =
                client.montarLembrete( entrada.frase( ), agora, categoriaQueryPort.listar( ) );

        validar( lembrete );

        return lembrete;
    }

    /**
     * Sem isto, uma frase que o modelo interpreta mal vira só a mensagem de erro do domínio, e não
     * há como saber o que ele montou. No demo eu não estarei olhando.
     */
    private void registrarRecusa( RuntimeException e, LembreteInputDTO lembrete )
    {
        LOGGER.warn( "Lembrete montado pela IA recusado pelo domínio ({}): {}", e.getMessage( ), lembrete );
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
