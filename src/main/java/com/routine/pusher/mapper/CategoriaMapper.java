package com.routine.pusher.mapper;

import com.routine.pusher.model.dto.CategoriaDTO;
import com.routine.pusher.model.entities.CategoriaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CategoriaMapper
{
    CategoriaMapper INSTANCE = Mappers.getMapper( CategoriaMapper.class );

    CategoriaDTO toDto( CategoriaEntity categoria );

    CategoriaEntity toEntity( CategoriaDTO categoria );

    void atualizaEntidade( CategoriaDTO dto, @MappingTarget CategoriaEntity entity );
}
