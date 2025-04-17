package com.routine.pusher.application.service;

import com.routine.pusher.application.service.interfaces.LembreteService;
import com.routine.pusher.data.mapper.LembreteMapper;
import com.routine.pusher.data.model.dto.LembreteInputDTO;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.data.model.enums.EnumStatusConclusao;
import com.routine.pusher.data.repository.LembreteRepository;
import com.routine.pusher.infrastructure.common.shared.SortInfo;
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

        return mapper.toOutputDto( repository.save( mapper.toEntity( inputDto ) ) );
    }

    @Override
    public LembreteOutputDTO concluir( Long id )
    {
        return repository.findById( id ).stream( )
                .map(entidade -> repository.save( entidade.concluirLembrete( ) ) )
                .map( mapper::toOutputDto ).toList( ).get( 0 );
    }

    @Override
    public List<LembreteOutputDTO> listar( String campoOrdenador, boolean ordemReversa )
    {
        LOGGER.debug("Listando lembretes por: {}", campoOrdenador);

        return repository.findAll( ).stream( )
                .map( mapper::toOutputDto )
                .sorted( new SortInfo<>( campoOrdenador, ordemReversa ) )
                .toList();
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
