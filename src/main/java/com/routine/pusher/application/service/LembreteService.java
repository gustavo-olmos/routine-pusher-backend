package com.routine.pusher.application.service;

import com.routine.pusher.application.job.AgendadorJob;
import com.routine.pusher.application.usecase.AtualizarDetalhesUseCase;
import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.application.usecase.ConcluirUseCase;
import com.routine.pusher.core.domain.categoria.port.CategoriaQueryPort;
import com.routine.pusher.core.domain.feriado.port.FeriadoPort;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import com.routine.pusher.core.domain.lembrete.LembreteMapper;
import com.routine.pusher.core.domain.lembrete.LembreteRepository;
import com.routine.pusher.core.domain.lembrete.dto.LembreteDetalhesInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.sessao.SessaoAnonima;
import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
import com.routine.pusher.infrastructure.common.shared.SortInfo;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.infrastructure.exceptions.LimiteDeUsoException;
import com.routine.pusher.infrastructure.exceptions.ProcessoException;
import com.routine.pusher.infrastructure.exceptions.StrategyException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LembreteService implements CRUDUseCase<LembreteInputDTO, LembreteOutputDTO, UUID>, ConcluirUseCase<UUID>,
        AtualizarDetalhesUseCase<LembreteDetalhesInputDTO, LembreteOutputDTO, UUID>
{
    private final Logger LOGGER = LoggerFactory.getLogger( LembreteService.class );

    private final LembreteMapper mapper;
    private final LembreteRepository repository;
    private final CategoriaQueryPort categoriaQueryPort;
    private final AgendadorJob agendadorJob;
    private final FeriadoPort feriadoPort;
    private final SessaoAtualPort sessaoAtual;


    @Override
    public LembreteOutputDTO adicionar( LembreteInputDTO inputDto )
    {
        LOGGER.debug("Adicionando lembrete");

        validarLimiteDaSessao( );

        Lembrete lembrete = mapper.toDomain( inputDto );
        lembrete.setSessaoUuid( sessaoAtual.uuid( ) );
        lembrete.setCategoria( categoriaQueryPort.buscarPorId( inputDto.categoriaId( ) ) );

        validarPoliticaDeCalendario( lembrete );
        lembrete.setExecucao( feriadoPort );
        validarAgendamento( lembrete );

        lembrete = mapper.toDomain( repository.save( mapper.toEntity( lembrete ) ) );

        try {
            agendadorJob.agendar( lembrete );
        }
        catch ( Exception e ) {
            repository.delete( mapper.toEntity( lembrete ) );
            throw new ProcessoException( e.getLocalizedMessage( ), e.getMessage( ) );
        }

        return mapper.toOutputDto( lembrete );
    }

    @Override
    public List<LembreteOutputDTO> listar( String campoOrdenador, boolean ordemReversa )
    {
        LOGGER.debug("Listando lembretes por: {}", campoOrdenador);

        // Passa pelo domínio antes de serializar: a projeção das próximas execuções é comportamento
        // do lembrete, e a entidade sozinha não sabe respondê-la.
        return repository.findBySessao_Uuid( sessaoAtual.uuid( ) ).stream( )
                .map( mapper::toDomain )
                .map( mapper::toOutputDto )
                .sorted( new SortInfo<>( LembreteOutputDTO.class, campoOrdenador, ordemReversa ) )
                .toList( );
    }

    @Override
    public LembreteOutputDTO atualizar( UUID uuid, LembreteInputDTO inputDTO )
    {
        LOGGER.debug("Alterando lembrete de uuid {}", uuid);

        LembreteEntity entidade = buscarPorUuid( uuid );

        mapper.updateEntity( inputDTO, entidade );

        Lembrete lembrete = mapper.toDomain( entidade );

        // A categoria é resolvida aqui, e não no updateEntity, pela mesma razão que em adicionar():
        // o DTO traz um id e a entidade quer a associação. O MapStruct não faz essa ponte sozinho e
        // — por unmappedSourcePolicy = IGNORE — descartava categoriaId sem nem um warning de build,
        // devolvendo 200 com a categoria antiga. Ver o javadoc de updateEntity no LembreteMapper.
        lembrete.setCategoria( categoriaQueryPort.buscarPorId( inputDTO.categoriaId( ) ) );

        // Atualizar rearma o disparo (o reagendar no fim deste método), então o lembrete não pode
        // continuar marcado como concluído: ficaria notificando com status CONCLUIDO e projeção
        // vazia. Editar um lembrete concluído é pedir ele de volta.
        lembrete.reabrirSeConcluido( );

        validarPoliticaDeCalendario( lembrete );
        lembrete.setExecucao( feriadoPort );
        validarAgendamento( lembrete );
        lembrete = mapper.toDomain( repository.save( mapper.toEntity( lembrete ) ) );

        agendadorJob.reagendar( lembrete );

        return mapper.toOutputDto( lembrete );
    }

    /**
     * Edita título, descrição e categoria sem encostar no agendamento.
     * <p>
     * É um caminho próprio, e não um ramo dentro de {@link #atualizar}, de propósito: aquele método
     * sempre reagenda (cancela e recria o trigger), sempre recalcula {@code proximaExecucao} e
     * reabre o lembrete concluído. Para uma correção de título isso é efeito colateral puro — e o
     * risco de introduzir um "se mudou o horário" lá dentro é alto justamente porque agendamento e
     * projeção precisam continuar concordando.
     * <p>
     * Aqui nada disso acontece: o disparo que já existia segue de pé, e o status é preservado.
     * Editar o título de um lembrete concluído não o traz de volta.
     */
    @Override
    public LembreteOutputDTO atualizarDetalhes( UUID uuid, LembreteDetalhesInputDTO inputDTO )
    {
        LOGGER.debug("Alterando detalhes do lembrete de uuid {}", uuid);

        Lembrete lembrete = mapper.toDomain( buscarPorUuid( uuid ) );

        lembrete.setTitulo( inputDTO.titulo( ) );
        lembrete.setDescricao( inputDTO.descricao( ) );
        lembrete.setCategoria( categoriaQueryPort.buscarPorId( inputDTO.categoriaId( ) ) );

        lembrete = mapper.toDomain( repository.save( mapper.toEntity( lembrete ) ) );

        return mapper.toOutputDto( lembrete );
    }

    /**
     * Recusa na porta as duas combinações que a política de dia útil não sustenta.
     * <p>
     * A primeira é custo: recorrência abaixo de um dia geraria milhares de ocorrências a descartar
     * por projeção, com o tamanho do laço escolhido pelo usuário. A segunda é intenção: em
     * {@code datasEspecificadas} o usuário apontou o dia a dedo, e não cabe ao calendário
     * sobrescrever escolha explícita — além de o agendamento dessas datas não passar pela política,
     * o que faria a prévia divergir do disparo.
     */
    private void validarPoliticaDeCalendario( Lembrete lembrete )
    {
        Recorrencia recorrencia = lembrete.getRecorrencia( );
        if( recorrencia == null || !recorrencia.exigeDiaUtil( ) ) return;

        if( !recorrencia.politicaDiaUtilEhCompativel( ) )
            throw new StrategyException( "Política de dia útil exige recorrência por calendário "
                    + "(dias da semana, dias fixos ou posição no mês) ou intervalo de ao menos um dia" );

        List<LocalDateTime> datasEspecificadas = lembrete.getNotificacao( ) != null
                ? lembrete.getNotificacao( ).getDatasEspecificadas( )
                : null;

        if( datasEspecificadas != null && !datasEspecificadas.isEmpty( ) )
            throw new StrategyException( "Política de dia útil não se aplica a datas específicas: "
                    + "as datas informadas já são a escolha do usuário" );
    }

    @Override
    public void concluir( UUID uuid )
    {
        LOGGER.debug("Concluindo lembrete de uuid {}", uuid);

        Lembrete lembrete = mapper.toDomain( buscarPorUuid( uuid ) );

        lembrete.concluirLembrete( );
        repository.save( mapper.toEntity( lembrete ) );

        agendadorJob.cancelar( uuid );
    }

    /**
     * Cancela o agendamento antes de apagar a linha, e não depois: se o cancelamento falhar, o
     * lembrete continua existindo e a operação é repetível. Na ordem inversa a falha deixaria um
     * trigger órfão — agendado para uma linha que já não existe.
     * <p>
     * O cancelamento faltava por completo aqui, embora {@link #concluir} e a faxina de sessão já o
     * fizessem. Cada exclusão vazava um trigger em WAITING; o rastro estava no banco de produção
     * (um trigger para zero lembretes) e no local (doze).
     * <p>
     * A busca vem primeiro de propósito: é ela que confina a operação à sessão do visitante. Cancelar
     * antes de validar a posse deixaria qualquer um desagendar o lembrete de outro pelo UUID.
     */
    @Override
    public void excluir( UUID uuid )
    {
        LOGGER.debug("Excluindo lembrete");

        LembreteEntity entidade = buscarPorUuid( uuid );

        agendadorJob.cancelar( uuid );
        repository.delete( entidade );
    }

    /**
     * Recusa o lembrete que nunca dispararia — porque não trouxe nenhuma regra de quando notificar,
     * ou porque a série que trouxe já se esgotou (datas todas no passado, validade vencida).
     * <p>
     * Antes disto, o primeiro caso estourava NullPointerException lá no domínio e virava 500. Vale
     * para qualquer origem: quem descobriu foi o chat de IA com uma frase sem sentido, mas um POST
     * humano sem recorrência e sem data cai exatamente no mesmo lugar.
     */
    private void validarAgendamento( Lembrete lembrete )
    {
        if( lembrete.getNotificacao( ) != null
                && lembrete.getNotificacao( ).getProximaExecucao( ) != null ) return;

        throw new StrategyException( "Não foi possível determinar quando notificar: informe uma "
                + "recorrência (intervalo, dias da semana ou dias do mês) ou datas específicas "
                + "no futuro" );
    }

    /**
     * O teto por sessão é a trava de custo do demo: o visitante que resolve estressar o motor de
     * agendamento esbarra aqui antes de virar carga. Vale só para criação — atualizar e concluir não
     * aumentam o acervo.
     */
    private void validarLimiteDaSessao( )
    {
        if( repository.countBySessao_Uuid( sessaoAtual.uuid( ) ) >= SessaoAnonima.LIMITE_LEMBRETES )
            throw new LimiteDeUsoException( "Limite de " + SessaoAnonima.LIMITE_LEMBRETES
                    + " lembretes por sessão atingido: exclua ou conclua um lembrete para criar outro" );
    }

    /**
     * Único ponto de entrada para localizar um lembrete, e sempre no escopo da sessão da requisição:
     * o UUID de outro visitante responde 404, sem revelar que existe. A chave da tabela é Long, mas
     * nada procura por ela — quem chega de fora traz UUID.
     */
    private LembreteEntity buscarPorUuid( UUID uuid )
    {
        return repository.findByUuidAndSessao_Uuid( uuid, sessaoAtual.uuid( ) )
                .orElseThrow( () -> new EntityNotFoundException( "Lembrete não encontrado para o uuid " + uuid ) );
    }
}
