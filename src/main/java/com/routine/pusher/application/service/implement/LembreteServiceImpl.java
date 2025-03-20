package com.routine.pusher.application.service.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.routine.pusher.infrastructure.exceptions.ConversaoLembreteException;
import com.routine.pusher.data.mapper.LembreteMapper;
import com.routine.pusher.data.model.dto.LembreteInputDTO;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.data.model.enums.StatusConclusao;
import com.routine.pusher.data.repository.LembreteRepository;
import com.routine.pusher.application.interfaces.client.OpenAIChatClient;
import com.routine.pusher.application.service.AgendadorService;
import com.routine.pusher.application.service.LembreteService;
import com.routine.pusher.infrastructure.common.shared.SortInfo;
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
    private final AgendadorService agendador;
    private final LembreteRepository repository;
    private final OpenAIChatClient clientService;


    @Override
    public LembreteOutputDTO adicionar( LembreteInputDTO dto )
    {
        LOGGER.debug("Adicionando lembrete");

        agendador.agendar( dto );
        return mapper.toDto( repository.save( mapper.toEntity( dto ) ) );
    }

    @Override
    public LembreteOutputDTO adicionarViaIA( String frase )
    {
        try {
            return clientService.buildLembreteChat( frase );
        }
        catch ( JsonProcessingException | ConversaoLembreteException ex ) {
            throw new RuntimeException( ex );
        }
    }

    @Override
    public List<LembreteOutputDTO> listar(String campoOrdenador, boolean ordemReversa )
    {
        LOGGER.debug("Listando lembretes por: {}", campoOrdenador);

        return repository.findAll( ).stream( )
                .map( mapper::toDto )
                .sorted( new SortInfo<>( campoOrdenador, ordemReversa ) )
                .toList();
    }

    @Override
    public String concluir( Long id )
    {
        LOGGER.debug("Concluindo lembrete de id {}", id);

        repository.findById( id )
                  .map( entidade -> {
                      entidade.setStatus(StatusConclusao.CONCLUIDO);
                      return repository.save(entidade);
                  });

        return "Lembrete Concluído";
    }


    @Override
    public LembreteOutputDTO atualizar( Long id, LembreteInputDTO dto )
    {
        LOGGER.debug("Alterando lembrete");

        agendador.agendar( dto );
        return repository.findById( id )
                .map( entidade -> {
                    mapper.atualizaEntidade( dto, entidade );
                    return repository.save( entidade );
                } )
                .map(mapper::toDto)
                .orElseThrow( ( ) -> new EntityNotFoundException( "Lembrete não encontrada para o id: " + id ) );
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
