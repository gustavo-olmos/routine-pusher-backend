package com.routine.pusher.data.mapper;

import com.routine.pusher.data.model.domain.Lembrete;
import com.routine.pusher.data.model.dto.LembreteInputDTO;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.data.model.entities.LembreteEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", builder = @Builder( disableBuilder = true ))
public interface LembreteMapper
{
    LembreteMapper INSTANCE = Mappers.getMapper( LembreteMapper.class );

    @Mapping(target = "categoria.id", source = "categoriaId")
    Lembrete toDomain( LembreteInputDTO inputDto );
    Lembrete toDomain( LembreteEntity entity );

    LembreteOutputDTO toOutputDto( Lembrete lembrete );
    LembreteOutputDTO toOutputDto( LembreteEntity entity );

    LembreteEntity toEntity( Lembrete lembrete );
    LembreteEntity updateEntity( Lembrete lembrete, @MappingTarget LembreteEntity entity );
}
