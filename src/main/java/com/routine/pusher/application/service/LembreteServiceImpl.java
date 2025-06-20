package com.routine.pusher.application.service;

import com.routine.pusher.application.service.interfaces.LembreteService;
import com.routine.pusher.core.domain.lembrete.LembreteMapper;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import com.routine.pusher.core.domain.lembrete.LembreteRepository;
import com.routine.pusher.infrastructure.common.shared.SortInfo;
import com.routine.pusher.infrastructure.exceptions.ProcessoException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class LembreteServiceImpl implements LembreteService
{
    private final Logger LOGGER = LoggerFactory.getLogger( LembreteServiceImpl.class );

    private final LembreteMapper mapper;
    private final LembreteRepository repository;


    @Override
    public LembreteOutputDTO salvar( LembreteInputDTO inputDto )
    {
        LOGGER.debug("Adicionando lembrete");

        Lembrete lembrete = mapper.toDomain( inputDto );
        LembreteEntity entidade = repository.save( mapper.toEntity( lembrete ) );
        lembrete.setId( entidade.getId( ) );

        try {
            lembrete.agendarLembrete( );
        } catch ( Exception ex ) {
            repository.deleteById( entidade.getId( ) );
            throw new ProcessoException( "Agendamento", ex.getMessage( ) );
        }

        return mapper.toOutputDto( entidade );
    }
    
    @Override
    public LembreteOutputDTO atualizar( Long id, LembreteInputDTO inputDTO )
    {
        LOGGER.debug("Alterando lembrete de id {}", id);

        Lembrete lembrete = mapper.toDomain( inputDTO );

        return repository.findById( id )
                .map( entidade -> mapper.updateEntity( lembrete, entidade ) )
                .map( repository::save )
                .map( mapper::toOutputDto )
                .orElseThrow( ( ) -> new EntityNotFoundException("Lembrete não encontrado para o id" + id) );
    }

    @Override
    public void concluir( Long id )
    {
        Lembrete lembrete = mapper.toDomain( repository.findById( id ).orElse( null ) );
        lembrete.concluirLembrete( );
        repository.save( mapper.toEntity( lembrete ) );
    }

    @Override
    public List<LembreteOutputDTO> listar( String campoOrdenador, boolean ordemReversa )
    {
        LOGGER.debug("Listando lembretes por: {}", campoOrdenador);

        return repository.findAll( ).stream( )
                .map( mapper::toOutputDto )
                .sorted( new SortInfo<>( campoOrdenador, ordemReversa ) )
                .toList( );
    }

    @Override
    public boolean excluir( Long id )
    {
        LOGGER.debug("Excluindo lembrete");

        if( repository.existsById( id ) ) {
            repository.deleteById( id );
            return true;
        }
        return false;
    }
}
