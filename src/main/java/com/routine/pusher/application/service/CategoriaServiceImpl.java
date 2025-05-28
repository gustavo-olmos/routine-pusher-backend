package com.routine.pusher.application.service;

import com.routine.pusher.application.service.interfaces.CategoriaService;
import com.routine.pusher.core.domain.categoria.CategoriaMapper;
import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import com.routine.pusher.core.domain.categoria.CategoriaRepository;
import com.routine.pusher.infrastructure.common.shared.SortInfo;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaServiceImpl implements CategoriaService
{
    private final Logger LOGGER = LoggerFactory.getLogger( CategoriaServiceImpl.class );

    private final CategoriaMapper mapper;
    private final CategoriaRepository repository;

    public CategoriaServiceImpl( CategoriaMapper mapper, CategoriaRepository repository )
    {
        this.mapper = mapper;
        this.repository = repository;
    }


    @Override
    public CategoriaOutputDTO adicionar( CategoriaInputDTO inputDto )
    {
        LOGGER.debug("Adicionando categoria");

        return mapper.toOutputDto( repository.save( mapper.toEntity( inputDto ) ) );
    }

    @Override
    public List<CategoriaOutputDTO> listar( String campoOrdenador, boolean ordemReversa )
    {
        LOGGER.debug("Listando categorias por: {}", campoOrdenador);

        return repository.findAll( ).stream( )
                         .map( mapper::toOutputDto )
                         .sorted( new SortInfo<>( campoOrdenador, ordemReversa ) )
                         .toList();
    }

    @Override
    public CategoriaOutputDTO atualizar( Long id, CategoriaInputDTO inputDto )
    {
        LOGGER.debug("Alterando categoria");

        return repository.findById( id )
                .map( entidade -> mapper.updateEntity( inputDto, entidade ) )
                .map( repository::save )
                .map( mapper::toOutputDto )
                .orElseThrow( ( ) -> new EntityNotFoundException("Categoria não encontrada para o id: " + id) );
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
