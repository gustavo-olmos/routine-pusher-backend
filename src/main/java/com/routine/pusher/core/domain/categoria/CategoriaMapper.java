package com.routine.pusher.core.domain.categoria;

import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CategoriaMapper
{
    CategoriaOutputDTO toOutputDto( CategoriaEntity entity );

    Categoria toDomain( CategoriaEntity entity );

    /** A sessão não vem do corpo: quem a resolve é o serviço, a partir do cookie da requisição. */
    @Mapping(target = "sessao", ignore = true)
    CategoriaEntity toEntity( CategoriaInputDTO categoria );

    /** Editar categoria nunca troca o dono — ver a nota em {@link #toEntity}. */
    @Mapping(target = "sessao", ignore = true)
    CategoriaEntity updateEntity(CategoriaInputDTO dto, @MappingTarget CategoriaEntity entity );
}
