package com.routine.pusher.mapper;

import com.routine.pusher.model.dto.SubtarefaDTO;
import com.routine.pusher.model.entities.SubtarefaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SubtarefaMapper
{
    SubtarefaMapper INSTANCE = Mappers.getMapper( SubtarefaMapper.class );


    SubtarefaDTO toDto( SubtarefaEntity subtarefa );

    SubtarefaEntity toEntity( SubtarefaDTO subtarefa );

    void atualizaEntidade( SubtarefaDTO dto, @MappingTarget SubtarefaEntity entity );
}
