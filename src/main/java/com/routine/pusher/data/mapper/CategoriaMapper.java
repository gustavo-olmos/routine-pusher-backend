package com.routine.pusher.data.mapper;

import com.routine.pusher.data.model.dto.CategoriaInputDTO;
import com.routine.pusher.data.model.dto.CategoriaOutputDTO;
import com.routine.pusher.data.model.entities.CategoriaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CategoriaMapper
{
    CategoriaMapper INSTANCE = Mappers.getMapper( CategoriaMapper.class );

    CategoriaOutputDTO toDto( CategoriaEntity categoria );

    CategoriaEntity toEntity( CategoriaInputDTO categoria );

    void atualizaEntidade( CategoriaInputDTO dto, @MappingTarget CategoriaEntity entity );
}
