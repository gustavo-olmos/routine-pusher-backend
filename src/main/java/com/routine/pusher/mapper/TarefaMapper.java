package com.routine.pusher.mapper;

import com.routine.pusher.model.dto.TarefaDTO;
import com.routine.pusher.model.entities.TarefaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TarefaMapper
{
    TarefaMapper INSTANCE = Mappers.getMapper( TarefaMapper.class );


    @Mapping(source = "comentario", target = "descricao")
    TarefaDTO toDto( TarefaEntity tarefa );

    @Mapping(source = "descricao", target = "comentario")
    TarefaEntity toEntity(TarefaDTO tarefa );

    void atualizaEntidade( TarefaDTO dto, @MappingTarget TarefaEntity entity );
}
