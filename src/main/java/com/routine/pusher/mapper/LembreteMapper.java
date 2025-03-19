package com.routine.pusher.mapper;

import com.routine.pusher.model.dto.LembreteInputDTO;
import com.routine.pusher.model.dto.LembreteOutputDTO;
import com.routine.pusher.model.entities.LembreteEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", builder = @Builder( disableBuilder = true ))
public interface LembreteMapper
{
    LembreteMapper INSTANCE = Mappers.getMapper( LembreteMapper.class );

    LembreteOutputDTO toDto( LembreteEntity lembrete );

    LembreteEntity toEntity( LembreteInputDTO lembrete );

    void atualizaEntidade( LembreteInputDTO dto, @MappingTarget LembreteEntity entity );
}
