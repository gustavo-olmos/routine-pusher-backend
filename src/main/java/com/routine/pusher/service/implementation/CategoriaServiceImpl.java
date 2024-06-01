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
import java.util.stream.Stream;

@Service
public class CategoriaServiceImpl implements CategoriaService
{
    private final Logger LOGGER = LoggerFactory.getLogger( CategoriaServiceImpl.class );

    private CategoriaRepository repository;
    private CategoriaMapper mapper;

    public CategoriaServiceImpl( CategoriaRepository repository )
    {
        this.repository = repository;
    }


    @Override
    public List<CategoriaDTO> listar( SortInfo sortInfo )
    {
        LOGGER.debug("Listando categorias por: {}", sortInfo);

        return List.of();
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
