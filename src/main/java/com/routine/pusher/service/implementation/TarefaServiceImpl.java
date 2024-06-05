package com.routine.pusher.service.implementation;

import com.routine.pusher.mapper.TarefaMapper;
import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.model.dto.TarefaDTO;
import com.routine.pusher.repository.TarefaRepository;
import com.routine.pusher.service.interfaces.TarefaService;
import com.routine.pusher.util.SortInfo;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TarefaServiceImpl implements TarefaService
{
    private final Logger LOGGER = LoggerFactory.getLogger( TarefaServiceImpl.class );

    private TarefaRepository repository;
    private TarefaMapper mapper;

    public TarefaServiceImpl( TarefaRepository repository, TarefaMapper mapper )
    {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<TarefaDTO> listar( )
    {
        LOGGER.debug("Listando tarefas");

        return repository.findAll( )
                         .stream( )
                         .map( mapper::toDto )
                         .collect( Collectors.toList( ) );
    }

    @Override
    public TarefaDTO adicionar( TarefaDTO dto )
    {
        LOGGER.debug("Adicionando tarefa");

        return Stream.of( dto )
                .map( mapper::toEntity )
                .map( repository::save )
                .map( mapper::toDto )
                .toList( ).get( 0 );
    }

    @Override
    public TarefaDTO editar( Long id, TarefaDTO dto )
    {
        LOGGER.debug("Alterando tarefa");

        return repository.findById( id )
                .map( entidade -> {
                    mapper.atualizaEntidade( dto, entidade );
                    repository.save( entidade );
                    return dto;
                } )
                .orElseThrow( ( ) ->
                        new EntityNotFoundException( "Tarefa não encontrada para o id: " + id ) );
    }

    @Override
    public boolean excluir( Long id )
    {
        LOGGER.debug("Excluindo tarefa");

        if( repository.existsById( id ) ) {
            repository.deleteById( id );
            return true;
        }
        return false;
    }
}
