package com.routine.pusher.service.implementation;

import com.routine.pusher.mapper.CategoriaMapper;
import com.routine.pusher.model.dto.CategoriaDTO;
import com.routine.pusher.repository.CategoriaRepository;
import com.routine.pusher.service.interfaces.CategoriaService;
import com.routine.pusher.util.SortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
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


    @SuppressWarnings("unchecked")
    @Override
    public List<CategoriaDTO> listar( String atributo, boolean ordemReversa )
    {
        LOGGER.debug("Listando categorias por: {}", atributo);

        return (List<CategoriaDTO>) repository.findAll( )
                .stream( )
                .map( mapper::toDto )
                .sorted( new SortInfo<CategoriaDTO>( atributo, ordemReversa ) )
                .collect( Collectors.toList( ) );
    }

    @Override
    public CategoriaDTO adicionar( CategoriaDTO dto )
    {
        LOGGER.debug("Adicionando categoria");

        return (CategoriaDTO) Stream.of( dto )
                .map( mapper::toEntity )
                .map( repository::save )
                .map( mapper::toDto );
    }

    @Override
    public CategoriaDTO editar( CategoriaDTO dto )
    {
        LOGGER.debug("Alterando categoria");

        return (CategoriaDTO) Stream.of( dto )
            .map( mapper::toEntity )
            .peek( categoria -> repository.findById( categoria.getId() ) )
            .map( repository::save )
            .map( mapper::toDto );
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
