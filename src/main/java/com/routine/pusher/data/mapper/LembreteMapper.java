package com.routine.pusher.data.mapper;

import com.routine.pusher.data.model.dto.LembreteInputDTO;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.data.model.dto.RecorrenciaInputDTO;
import com.routine.pusher.data.model.entities.LembreteEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", builder = @Builder( disableBuilder = true ))
public interface LembreteMapper
{
    LembreteMapper INSTANCE = Mappers.getMapper( LembreteMapper.class );

    LembreteOutputDTO toOutputDto( LembreteEntity lembrete );

    @Mapping(target = "categoria.id", source = "categoriaId")
    @Mapping(target = "recorrencia.id", source = "recorrenciaId")
    LembreteEntity toEntity( LembreteInputDTO lembrete );
}
