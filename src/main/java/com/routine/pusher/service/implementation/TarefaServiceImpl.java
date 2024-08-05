package com.routine.pusher.service.implementation;

import com.routine.pusher.mapper.TarefaMapper;
import com.routine.pusher.model.dto.TarefaDTO;
import com.routine.pusher.repository.TarefaRepository;
import com.routine.pusher.service.interfaces.TarefaService;
import com.routine.pusher.util.SortInfo;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TarefaServiceImpl implements TarefaService
{
    private final Logger LOGGER = LoggerFactory.getLogger( TarefaServiceImpl.class );

    private TarefaRepository repository;
    private TarefaMapper mapper;

    public TarefaServiceImpl(TarefaRepository repository, TarefaMapper mapper )
    {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public TarefaDTO adicionar( TarefaDTO dto )
    {
        LOGGER.debug("Adicionando subtarefa");

        return mapper.toDto( repository.save( mapper.toEntity( dto ) ) );
    }

    @Override
    public TarefaDTO atualizar( Long id, TarefaDTO dto )
    {
        LOGGER.debug("Alterando subtarefa");

        return repository.findById( id ).stream( )
            .peek( entidade -> mapper.atualizaEntidade( dto, entidade ) )
            .map( repository::save )
            .map( mapper::toDto )
            .toList( ).get( 0 );
    }

    @Override
    public boolean excluir( Long id )
    {
        LOGGER.debug("Excluindo categoria");

        if( repository.existsById( id ) ) {
            repository.deleteById( id );
            return true;
        }
        return false;
    }
}
