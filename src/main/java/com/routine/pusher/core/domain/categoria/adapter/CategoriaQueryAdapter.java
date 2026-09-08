package com.routine.pusher.core.domain.categoria.adapter;

import com.routine.pusher.core.domain.categoria.Categoria;
import com.routine.pusher.core.domain.categoria.CategoriaMapper;
import com.routine.pusher.core.domain.categoria.CategoriaRepository;
import com.routine.pusher.core.domain.categoria.port.CategoriaQueryPort;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class CategoriaQueryAdapter implements CategoriaQueryPort
{
    private final CategoriaMapper mapper;
    private final CategoriaRepository repository;

    /**
     * Falha alto quando o id não existe, em vez de devolver {@code null}. Com o {@code orElse(null)}
     * anterior a categoria inexistente atravessava o serviço em silêncio e só era barrada pela
     * constraint {@code categoria_id NOT NULL} lá no INSERT — o que virava um 409 "conflita com
     * dados já existentes", mensagem que aponta para o lugar errado. Quem informou um id inválido
     * merece 404 dizendo exatamente isso.
     */
    @Override
    public Categoria buscarPorId( Long id )
    {
        return repository.findById( id )
                .map( mapper::toDomain )
                .orElseThrow( () -> new EntityNotFoundException( "Categoria não encontrada para o id " + id ) );
    }

    @Override
    public List<Categoria> listar( )
    {
        return repository.findAll( ).stream( )
                .map( mapper::toDomain )
                .toList( );
    }
}
