package com.routine.pusher.service.implementation;

import com.routine.pusher.mapper.SubtarefaMapper;
import com.routine.pusher.model.dto.SubtarefaDTO;
import com.routine.pusher.repository.SubTarefaRepository;
import com.routine.pusher.service.interfaces.SubtarefaService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SubtarefaServiceImpl implements SubtarefaService
{
    private final Logger LOGGER = LoggerFactory.getLogger( SubtarefaServiceImpl.class );

    private SubTarefaRepository repository;
    private SubtarefaMapper mapper;

    public SubtarefaServiceImpl( SubTarefaRepository repository, SubtarefaMapper mapper )
    {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<SubtarefaDTO> listar( )
    {
        LOGGER.debug("Listando subtarefas");

        return repository.findAll( )
                .stream( )
                .map( mapper::toDto )
                .collect( Collectors.toList( ) );
    }

    @Override
    public SubtarefaDTO adicionar( SubtarefaDTO dto )
    {
        LOGGER.debug("Adicionando subtarefa");

        return Stream.of( dto )
                .map( mapper::toEntity )
                .map( repository::save )
                .map( mapper::toDto )
                .toList( ).get( 0 );
    }

    @Override
    public SubtarefaDTO editar( Long id, SubtarefaDTO dto )
    {
        LOGGER.debug("Alterando subtarefa");

        return repository.findById( id )
                .map( entidade -> {
                    mapper.atualizaEntidade( dto, entidade );
                    repository.save( entidade );
                    return dto;
                } )
                .orElseThrow( ( ) ->
                        new EntityNotFoundException( "Subtarefa não encontrada para o id: " + id ) );
    }

    @Override
    public boolean excluir( Long id )
    {
        LOGGER.debug("Excluindo subtarefa");

        if( repository.existsById( id ) ) {
            repository.deleteById( id );
            return true;
        }
        return false;
    }
}
