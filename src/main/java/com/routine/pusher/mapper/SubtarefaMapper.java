package com.routine.pusher.mapper;

import com.routine.pusher.model.dto.SubtarefaDTO;
import com.routine.pusher.model.entities.SubtarefaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubtarefaMapper
{
    SubtarefaDTO entityToDto( SubtarefaEntity subtarefa );

    SubtarefaEntity dtoToEntity( SubtarefaDTO subtarefa );
}
