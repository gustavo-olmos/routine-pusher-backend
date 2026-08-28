package com.routine.pusher.application.service;

import com.routine.pusher.application.job.AgendadorJob;
import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.application.usecase.ConcluirUseCase;
import com.routine.pusher.core.domain.categoria.port.CategoriaQueryPort;
import com.routine.pusher.core.domain.feriado.port.FeriadoPort;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import com.routine.pusher.core.domain.lembrete.LembreteMapper;
import com.routine.pusher.core.domain.lembrete.LembreteRepository;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.infrastructure.common.shared.SortInfo;
import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import com.routine.pusher.infrastructure.exceptions.ProcessoException;
import com.routine.pusher.infrastructure.exceptions.StrategyException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class LembreteService implements CRUDUseCase<LembreteInputDTO, LembreteOutputDTO>, ConcluirUseCase
{
    private final Logger LOGGER = LoggerFactory.getLogger( LembreteService.class );

    private final LembreteMapper mapper;
    private final LembreteRepository repository;
    private final CategoriaQueryPort categoriaQueryPort;
    private final AgendadorJob agendadorJob;
    private final FeriadoPort feriadoPort;


    @Override
    public LembreteOutputDTO adicionar( LembreteInputDTO inputDto )
    {
        LOGGER.debug("Adicionando lembrete");

        Lembrete lembrete = mapper.toDomain( inputDto );
        lembrete.setCategoria( categoriaQueryPort.buscarPorId( inputDto.categoriaId( ) ) );

        validarPoliticaDeCalendario( lembrete );
        lembrete.setExecucao( feriadoPort );

        lembrete = mapper.toDomain( repository.save( mapper.toEntity( lembrete ) ) );

        try {
            agendadorJob.agendar( lembrete );
        }
        catch ( Exception e ) {
            repository.deleteById( lembrete.getId( ) );
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
        return repository.findAll( ).stream( )
                .map( mapper::toDomain )
                .map( mapper::toOutputDto )
                .sorted( new SortInfo<>( campoOrdenador, ordemReversa ) )
                .toList( );
    }

    @Override
    public LembreteOutputDTO atualizar( Long id, LembreteInputDTO inputDTO )
    {
        LOGGER.debug("Alterando lembrete de id {}", id);

        LembreteEntity entidade = repository.findById( id )
                .orElseThrow( () -> new EntityNotFoundException("Lembrete não encontrado para o id" + id) );

        mapper.updateEntity( inputDTO, entidade );

        Lembrete lembrete = mapper.toDomain( entidade );

        validarPoliticaDeCalendario( lembrete );
        lembrete.setExecucao( feriadoPort );
        lembrete = mapper.toDomain( repository.save( mapper.toEntity( lembrete ) ) );

        agendadorJob.reagendar( lembrete );

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
    public void concluir( Long id )
    {
        LOGGER.debug("Concluindo lembrete de id {}", id);

        Lembrete lembrete = repository.findById( id )
                .map( mapper::toDomain )
                .orElseThrow( () -> new EntityNotFoundException("Lembrete não encontrado para o id" + id) );

        lembrete.concluirLembrete( );
        repository.save( mapper.toEntity( lembrete ) );

        agendadorJob.cancelar( id );
    }

    @Override
    public void excluir( Long id )
    {
        LOGGER.debug("Excluindo lembrete");

        if ( !repository.existsById( id ) )
            throw new EntityNotFoundException("Lembrete não encontrado para o id" + id);

        repository.deleteById( id );
    }
}
