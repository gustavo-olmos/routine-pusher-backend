package com.routine.pusher.mapper;

import com.routine.pusher.model.dto.TarefaDTO;
import com.routine.pusher.model.entities.TarefaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TarefaMapper
{
    TarefaMapper INSTANCE = Mappers.getMapper( TarefaMapper.class );


    @Mapping(source = "lembrete.id", target = "lembreteId")
    TarefaDTO toDto( TarefaEntity subtarefa );

    @Mapping(source = "lembreteId", target = "lembrete.id")
    TarefaEntity toEntity( TarefaDTO subtarefa );

    void atualizaEntidade( TarefaDTO dto, @MappingTarget TarefaEntity entity );
}
