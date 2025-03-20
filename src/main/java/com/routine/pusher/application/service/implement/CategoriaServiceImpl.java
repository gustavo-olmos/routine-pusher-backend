package com.routine.pusher.application.service.implement;

import com.routine.pusher.data.mapper.CategoriaMapper;
import com.routine.pusher.data.model.dto.CategoriaDTO;
import com.routine.pusher.data.repository.CategoriaRepository;
import com.routine.pusher.application.service.CategoriaService;
import com.routine.pusher.infrastructure.util.SortInfo;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<CategoriaDTO> listar( String campoOrdenador, boolean ordemReversa )
    {
        LOGGER.debug("Listando categorias por: {}", campoOrdenador);

        return repository.findAll( ).stream( )
                         .map( mapper::toDto )
                         .sorted( new SortInfo<>( campoOrdenador, ordemReversa ) )
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
