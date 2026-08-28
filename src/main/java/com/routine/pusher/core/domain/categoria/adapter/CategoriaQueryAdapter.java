package com.routine.pusher.core.domain.categoria.adapter;

import com.routine.pusher.core.domain.categoria.Categoria;
import com.routine.pusher.core.domain.categoria.CategoriaMapper;
import com.routine.pusher.core.domain.categoria.CategoriaRepository;
import com.routine.pusher.core.domain.categoria.port.CategoriaQueryPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class CategoriaQueryAdapter implements CategoriaQueryPort
{
    private final CategoriaMapper mapper;
    private final CategoriaRepository repository;

    @Override
    public Categoria buscarPorId( Long id )
    {
        return mapper.toDomain( repository.findById( id ).orElse( null ) );
    }

    @Override
    public List<Categoria> listar( )
    {
        return repository.findAll( ).stream( )
                .map( mapper::toDomain )
                .toList( );
    }
}
