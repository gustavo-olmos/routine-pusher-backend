package com.routine.pusher.core.domain.categoria;

import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CategoriaMapper
{
    CategoriaMapper INSTANCE = Mappers.getMapper( CategoriaMapper.class );

    CategoriaOutputDTO toOutputDto(CategoriaEntity categoria );

    CategoriaEntity toEntity( CategoriaInputDTO categoria );

    CategoriaEntity updateEntity(CategoriaInputDTO dto, @MappingTarget CategoriaEntity entity );
}
