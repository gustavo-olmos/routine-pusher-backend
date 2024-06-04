package com.routine.pusher.service.implementation;

import com.routine.pusher.mapper.CategoriaMapper;
import com.routine.pusher.model.dto.CategoriaDTO;
import com.routine.pusher.model.entities.CategoriaEntity;
import com.routine.pusher.repository.CategoriaRepository;
import com.routine.pusher.service.interfaces.CategoriaService;
import com.routine.pusher.util.SortInfo;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CategoriaServiceImpl implements CategoriaService
{
    private final Logger LOGGER = LoggerFactory.getLogger( CategoriaServiceImpl.class );

    private CategoriaRepository repository;
    private CategoriaMapper mapper;

    public CategoriaServiceImpl( CategoriaRepository repository, CategoriaMapper mapper )
    {
        this.repository = repository;
        this.mapper = mapper;
    }


    @Override
    public List<CategoriaDTO> listar( String atributo, boolean ordemReversa )
    {
        LOGGER.debug("Listando categorias por: {}", atributo);

        List<CategoriaDTO> categorias = new java.util.ArrayList<>(
                                        repository.findAll( )
                                        .stream( )
                                        .map( mapper::toDto )
                                        .collect( Collectors.toList( ) ) );

        categorias.sort( new SortInfo<CategoriaDTO>( atributo, ordemReversa ) );

        return categorias;
    }

    @Override
    public CategoriaDTO adicionar( CategoriaDTO dto )
    {
        LOGGER.debug("Adicionando categoria");

        return Stream.of( dto )
                .map( mapper::toEntity )
                .map( repository::save )
                .map( mapper::toDto )
                .toList( ).get( 0 );
    }

    @Override
    public CategoriaDTO editar( Long id, CategoriaDTO dto )
    {
        LOGGER.debug("Alterando categoria");

        return repository.findById( id )
                .map( entidade -> {
                    mapper.atualizaEntidade( dto, entidade );
                    repository.save( entidade );
                    return dto;
                } )
                .orElseThrow( ( ) ->
                        new EntityNotFoundException("Categoria não encontrada para o id: " + id ) );
    }

    @Override
    public boolean excluir( Long id )
    {
        LOGGER.debug("Excluindo categoria");

        if( repository.existsById( id ) ) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
