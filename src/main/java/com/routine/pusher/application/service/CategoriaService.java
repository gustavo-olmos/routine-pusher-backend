package com.routine.pusher.application.service;

import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.core.domain.categoria.CategoriaMapper;
import com.routine.pusher.core.domain.categoria.CategoriaRepository;
import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import com.routine.pusher.core.domain.categoria.CategoriaEntity;
import com.routine.pusher.core.domain.lembrete.LembreteQueryPort;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaEntity;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaRepository;
import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
import com.routine.pusher.infrastructure.common.shared.SortInfo;
import com.routine.pusher.infrastructure.exceptions.ExclusaoException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CategoriaService implements CRUDUseCase<CategoriaInputDTO, CategoriaOutputDTO, Long>
{
    private final Logger LOGGER = LoggerFactory.getLogger( CategoriaService.class );

    private final CategoriaMapper mapper;
    private final CategoriaRepository repository;
    private final LembreteQueryPort lembreteQueryPort;
    private final SessaoAtualPort sessaoAtual;
    private final SessaoAnonimaRepository sessaoRepository;


    @Override
    public CategoriaOutputDTO adicionar( CategoriaInputDTO inputDto )
    {
        LOGGER.debug("Adicionando categoria");

        CategoriaEntity entidade = mapper.toEntity( inputDto );
        entidade.setSessao( sessaoDaRequisicao( ) );

        return mapper.toOutputDto( repository.save( entidade ) );
    }

    @Override
    public List<CategoriaOutputDTO> listar( String campoOrdenador, boolean ordemReversa )
    {
        LOGGER.debug("Listando categorias por: {}", campoOrdenador);

        return repository.findBySessao_Uuid( sessaoAtual.uuid( ) ).stream( )
                         .map( mapper::toOutputDto )
                         .sorted( new SortInfo<>( CategoriaOutputDTO.class, campoOrdenador, ordemReversa ) )
                         .toList();
    }

    @Override
    public CategoriaOutputDTO buscarPeloId( Long id )
    {
        LOGGER.debug("Buscando categoria de id: {}", id);

        return buscarNaSessao( id )
                .map( mapper::toOutputDto )
                .orElseThrow( () -> new EntityNotFoundException("Categoria não encontrada") );
    }

    @Override
    public CategoriaOutputDTO atualizar( Long id, CategoriaInputDTO inputDto )
    {
        LOGGER.debug("Alterando categoria");

        return buscarNaSessao( id )
                .map( entidade -> mapper.updateEntity( inputDto, entidade ) )
                .map( repository::save )
                .map( mapper::toOutputDto )
                .orElseThrow( ( ) -> new EntityNotFoundException("Categoria não encontrada para o id: " + id) );
    }

    @Override
    public void excluir( Long id )
    {
        LOGGER.debug("Excluindo categoria com id {}", id);

        CategoriaEntity entidade = buscarNaSessao( id )
                .orElseThrow( () -> new EntityNotFoundException("Categoria não encontrada para o id " + id) );

        if( lembreteQueryPort.existeLembreteComCategoriaId( id ) )
            throw new ExclusaoException("Não foi possível concluir a exclusão dessa categoria. Ainda restam lembretes associados");

        repository.delete( entidade );
    }

    /**
     * Único ponto de entrada para localizar uma categoria, e sempre no escopo da sessão da
     * requisição — mesmo contrato do {@code LembreteService.buscarPorUuid}: o id de outro visitante
     * responde 404, sem revelar que existe.
     */
    private Optional<CategoriaEntity> buscarNaSessao( Long id )
    {
        return repository.findByIdAndSessao_Uuid( id, sessaoAtual.uuid( ) );
    }

    private SessaoAnonimaEntity sessaoDaRequisicao( )
    {
        return sessaoRepository.findByUuid( sessaoAtual.uuid( ) )
                .orElseThrow( () -> new EntityNotFoundException( "Sessão não encontrada" ) );
    }
}
