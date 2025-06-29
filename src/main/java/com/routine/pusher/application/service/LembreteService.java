package com.routine.pusher.application.service;

import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.application.usecase.ConcluirUseCase;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.LembreteMapper;
import com.routine.pusher.core.domain.lembrete.LembreteRepository;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.lembrete.factory.LembreteFactory;
import com.routine.pusher.infrastructure.common.shared.SortInfo;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class LembreteService implements CRUDUseCase<LembreteInputDTO, LembreteOutputDTO>, ConcluirUseCase
{
    private final Logger LOGGER = LoggerFactory.getLogger( LembreteService.class );

    private final LembreteMapper mapper;
    private final LembreteFactory factory;
    private final LembreteRepository repository;


    @Override
    public LembreteOutputDTO adicionar( LembreteInputDTO inputDto )
    {
        LOGGER.debug("Adicionando lembrete");

        Lembrete lembrete = mapper.toDomain( inputDto );
        factory.construirLembrete( lembrete );

        return mapper.toOutputDto( repository.save( mapper.toEntity( lembrete ) ) );
    }

    @Override
    public List<LembreteOutputDTO> listar( String campoOrdenador, boolean ordemReversa )
    {
        LOGGER.debug("Listando lembretes por: {}", campoOrdenador);

        return repository.findAll( ).stream( )
                .map( mapper::toOutputDto )
                .sorted( new SortInfo<>( campoOrdenador, ordemReversa ) )
                .toList( );
    }

    @Override
    public LembreteOutputDTO buscarPeloId( Long id )
    {
        return null;
    }

    @Override
    public LembreteOutputDTO atualizar( Long id, LembreteInputDTO inputDTO )
    {
        LOGGER.debug("Alterando lembrete de id {}", id);

        return repository.findById( id )
                .map( entidade -> mapper.updateEntity( inputDTO, entidade ) )
                .map( repository::save )
                .map( mapper::toOutputDto )
                .orElseThrow( () -> new EntityNotFoundException("Lembrete não encontrado para o id" + id) );
    }

    @Override
    public void concluir( Long id )
    {
        Lembrete lembrete = mapper.toDomain( repository.findById( id ).orElse( null ) );
        lembrete.concluirLembrete( );
        repository.save( mapper.toEntity( lembrete ) );
    }

    @Override
    public void excluir( Long id )
    {
        LOGGER.debug("Excluindo lembrete");

        if ( repository.existsById( id ) ) {
            repository.deleteById( id );
        } else {
            throw new EntityNotFoundException("Lembrete não encontrado para o id" + id);
        }
    }
}
