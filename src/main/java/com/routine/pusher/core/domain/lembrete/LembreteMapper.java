package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.notificacao.NotificacaoEntity;
import com.routine.pusher.core.domain.recorrencia.RecorrenciaEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LembreteMapper
{
    @Mapping(target = "categoria.id", source = "categoriaId")
    Lembrete toDomain( LembreteInputDTO inputDto );
    Lembrete toDomain( LembreteEntity entity );

    LembreteOutputDTO toOutputDto( Lembrete lembrete );
    LembreteOutputDTO toOutputDto( LembreteEntity entity );

    LembreteEntity toEntity( Lembrete lembrete );

    LembreteEntity updateEntity( LembreteInputDTO inputDTO, @MappingTarget LembreteEntity entity );

    /**
     * Garante a back-reference do lado dono da FK: sem isto, ao salvar o LembreteEntity por cascata,
     * notificacao/recorrencia ficariam com lembrete_id nulo.
     */
    @AfterMapping
    default void vincularFilhos( @MappingTarget LembreteEntity entity )
    {
        NotificacaoEntity notificacao = entity.getNotificacao( );
        if( notificacao != null )
            notificacao.setLembrete( entity );

        RecorrenciaEntity recorrencia = entity.getRecorrencia( );
        if( recorrencia != null )
            recorrencia.setLembrete( entity );
    }
}
