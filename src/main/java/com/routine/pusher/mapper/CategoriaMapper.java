package com.routine.pusher.mapper;

import com.routine.pusher.model.dto.CategoriaDTO;
import com.routine.pusher.model.entities.CategoriaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoriaMapper
{
    CategoriaDTO toDto( CategoriaEntity categoria );

    CategoriaEntity toEntity( CategoriaDTO categoria );

    void atualizaEntidade( CategoriaDTO dto, @MappingTarget CategoriaEntity entity );
}
