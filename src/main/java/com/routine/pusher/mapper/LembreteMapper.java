package com.routine.pusher.mapper;

import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.model.entities.LembreteEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", builder = @Builder( disableBuilder = true ))
public interface LembreteMapper
{
    LembreteMapper INSTANCE = Mappers.getMapper( LembreteMapper.class );


    @Mapping(source = "tarefas", target = "tarefas")
    LembreteDTO toDto( LembreteEntity lembrete );

    @Mapping(source = "tarefas", target = "tarefas")
    LembreteEntity toEntity( LembreteDTO lembrete );

    void atualizaEntidade( LembreteDTO dto, @MappingTarget LembreteEntity entity );
}
