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

    private CategoriaMapper mapper;
    private CategoriaRepository repository;

    public CategoriaServiceImpl( CategoriaMapper mapper, CategoriaRepository repository )
    {
        this.mapper = mapper;
        this.repository = repository;
    }


    @Override
    public CategoriaDTO adicionar( CategoriaDTO dto )
    {
        LOGGER.debug("Adicionando categoria");

        return mapper.toDto( repository.save( mapper.toEntity( dto ) ) );
    }

    @Override
    public List<CategoriaDTO> listar( String atributo, boolean ordemReversa )
    {
        LOGGER.debug("Listando categorias por: {}", atributo);

        return repository.findAll( ).stream( )
                         .map( mapper::toDto )
                         .sorted( new SortInfo<CategoriaDTO>( atributo, ordemReversa ) )
                         .toList();
    }

    @Override
    public CategoriaDTO atualizar( Long id, CategoriaDTO dto )
    {
        LOGGER.debug("Alterando categoria");

        return repository.findById( id )
                .map( entidade -> {
                    mapper.atualizaEntidade( dto, entidade );
                    repository.save( entidade );
                    return dto;
                } )
                .orElseThrow( ( ) -> new EntityNotFoundException( "Categoria não encontrada para o id: " + id ) );
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
