package com.routine.pusher.data.mapper;

import com.routine.pusher.data.model.dto.CategoriaDTO;
import com.routine.pusher.data.model.entities.CategoriaEntity;
import org.mapstruct.Mapper;
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
