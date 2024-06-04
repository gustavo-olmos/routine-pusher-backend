package com.routine.pusher.mapper;

import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.model.entities.LembreteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LembreteMapper
{
//    LembreteDTO entityToDto( LembreteEntity lembrete );
//
//    LembreteEntity dtoToEntity( LembreteDTO lembrete );

    void atualizaEntidade( LembreteDTO dto, @MappingTarget LembreteEntity entity );
}
