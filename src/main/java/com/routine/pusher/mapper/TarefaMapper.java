package com.routine.pusher.mapper;

import com.routine.pusher.model.dto.TarefaDTO;
import com.routine.pusher.model.entities.TarefaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TarefaMapper
{
    @Mapping(source = "comentario", target = "descricao")
    TarefaDTO entityToDto( TarefaEntity tarefa );

    @Mapping(source = "descricao", target = "comentario")
    TarefaEntity dtoToEntity( TarefaDTO tarefa );

    void atualizaEntidade( TarefaDTO dto, @MappingTarget TarefaEntity entity );
}
