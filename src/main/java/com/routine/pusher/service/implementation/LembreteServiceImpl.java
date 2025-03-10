package com.routine.pusher.service.implementation;

import com.routine.pusher.mapper.LembreteMapper;
import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.repository.LembreteRepository;
import com.routine.pusher.service.interfaces.AgendadorService;
import com.routine.pusher.service.interfaces.LembreteService;
import com.routine.pusher.util.SortInfo;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class LembreteServiceImpl implements LembreteService
{
    private final Logger LOGGER = LoggerFactory.getLogger( LembreteServiceImpl.class );

    private final LembreteMapper mapper;
    private final AgendadorService agendador;
    private final LembreteRepository repository;

    public LembreteServiceImpl( LembreteMapper mapper, AgendadorService agendador, LembreteRepository repository )
    {
        this.mapper = mapper;
        this.agendador = agendador;
        this.repository = repository;
    }


    @Override
    public LembreteDTO adicionar( LembreteDTO dto )
    {
        LOGGER.debug("Adicionando lembrete");

        LembreteDTO lembrete = Stream.of( dto )
                .map( mapper::toEntity )
                .peek( entidade -> entidade.getTarefas( ).forEach( tarefa -> tarefa.setLembrete( entidade ) ) )
                .map( repository::save )
                .map( mapper::toDto )
                .toList().get( 0 );

        agendador.agendar(lembrete);
        return lembrete;
    }

    @Override
    public List<LembreteDTO> listar( String campoOrdenador, boolean ordemReversa )
    {
        LOGGER.debug("Listando lembretes por: {}", campoOrdenador);

        return repository.findAll( ).stream( )
                .map( mapper::toDto )
                .sorted( new SortInfo<>( campoOrdenador, ordemReversa ) )
                .toList();
    }

    @Override
    public LembreteDTO atualizar( Long id, LembreteDTO dto )
    {
        LOGGER.debug("Alterando lembrete");

        LembreteDTO lembrete = repository.findById( id )
                .map( entidade -> {
                    mapper.atualizaEntidade( dto, entidade );
                    entidade.getTarefas( ).forEach( tarefa -> tarefa.setLembrete( entidade ) );
                    repository.save( entidade );

                    return mapper.toDto( entidade );
                } )
                .orElseThrow( ( ) -> new EntityNotFoundException( "Lembrete não encontrada para o id: " + id ) );

        agendador.agendar( lembrete );
        return lembrete;
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
