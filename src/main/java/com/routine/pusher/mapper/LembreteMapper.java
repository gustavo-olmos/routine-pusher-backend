package com.routine.pusher.mapper;

import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.model.dto.TarefaDTO;
import com.routine.pusher.model.entities.LembreteEntity;
import com.routine.pusher.model.entities.TarefaEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", builder = @Builder( disableBuilder = true ))
public interface LembreteMapper
{
    LembreteMapper INSTANCE = Mappers.getMapper( LembreteMapper.class );


    @Mapping(target = "tarefas", expression = "java( mapTarefas( lembrete.getTarefas( ), lembrete.getId( ) ) )")
    LembreteDTO toDto( LembreteEntity lembrete );

    @Mapping(target = "tarefas", source = "tarefas")
    LembreteEntity toEntity( LembreteDTO lembrete );

    void atualizaEntidade( LembreteDTO dto, @MappingTarget LembreteEntity entity );

    default List<TarefaDTO> mapTarefas( List<TarefaEntity> tarefas, Long lembreteId ) {
        return tarefas.stream( )
                .map( tarefa -> new TarefaDTO( tarefa.getId( ), lembreteId, tarefa.getTitulo( ), tarefa.getStatus( ) ) )
                .collect( Collectors.toList( ) );
    }
}
