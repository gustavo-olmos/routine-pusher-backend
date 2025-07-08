package com.routine.pusher.core.domain.categoria;

import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CategoriaMapper
{
    CategoriaOutputDTO toOutputDto( CategoriaEntity entity );

    Categoria toDomain( CategoriaEntity entity );

    CategoriaEntity toEntity( CategoriaInputDTO categoria );

    CategoriaEntity updateEntity(CategoriaInputDTO dto, @MappingTarget CategoriaEntity entity );
}
