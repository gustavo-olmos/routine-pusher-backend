package com.routine.pusher.application.service;

import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.core.domain.categoria.CategoriaMapper;
import com.routine.pusher.core.domain.categoria.CategoriaRepository;
import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import com.routine.pusher.core.domain.lembrete.LembreteQueryPort;
import com.routine.pusher.infrastructure.common.shared.SortInfo;
import com.routine.pusher.infrastructure.exceptions.ExclusaoException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoriaService implements CRUDUseCase<CategoriaInputDTO, CategoriaOutputDTO>
{
    private final Logger LOGGER = LoggerFactory.getLogger( CategoriaService.class );

    private final CategoriaMapper mapper;
    private final CategoriaRepository repository;
    private final LembreteQueryPort lembreteQueryPort;


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
    public CategoriaOutputDTO buscarPeloId( Long id )
    {
        LOGGER.debug("Buscando categoria de id: {}", id);

        return repository.findById( id )
                .map( mapper::toOutputDto )
                .orElseThrow( () -> new EntityNotFoundException("Categoria não encontrada") );
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
    public void excluir( Long id )
    {
        LOGGER.debug("Excluindo categoria com id {}", id);

        if( !repository.existsById( id ) )
            throw new EntityNotFoundException("Categoria não encontrada para o id " + id);

        if( lembreteQueryPort.existeLembreteComCategoriaId( id ) )
            throw new ExclusaoException("Não foi possível concluir a exclusão dessa categoria. Ainda restam lembretes associados");

        repository.deleteById( id );
    }
}
