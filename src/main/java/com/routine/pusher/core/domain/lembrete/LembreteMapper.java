package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.core.domain.feriado.port.FeriadoPort;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.notificacao.NotificacaoEntity;
import com.routine.pusher.core.domain.recorrencia.RecorrenciaEntity;
import jakarta.inject.Inject;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Classe abstrata, e não interface, para poder receber o {@link FeriadoPort}: a projeção das
 * próximas execuções precisa dele, e é aqui que ela é montada. Sendo colaborador injetado, trocar a
 * origem dos feriados não toca em nada disto.
 */
@Mapper(componentModel = "spring")
public abstract class LembreteMapper
{
    /**
     * Nulo em teste unitário que instancia o Impl na mão — e nulo é justamente o caso "sem restrição
     * de calendário", então a projeção continua respondendo em vez de estourar.
     */
    @Inject
    protected FeriadoPort feriadoPort;

    @Mapping(target = "categoria.id", source = "categoriaId")
    public abstract Lembrete toDomain( LembreteInputDTO inputDto );

    public abstract Lembrete toDomain( LembreteEntity entity );

    /**
     * A projeção das próximas execuções sai daqui porque só o domínio sabe calculá-la — depende da
     * recorrência, da cota e do calendário, não de um campo do lembrete.
     */
    @Mapping(target = "proximasExecucoes",
             expression = "java( lembrete.calcularProximasExecucoes( Lembrete.LIMITE_PROXIMAS_EXECUCOES, feriadoPort ) )")
    public abstract LembreteOutputDTO toOutputDto( Lembrete lembrete );

    /**
     * A entidade é estado puro, sem comportamento de recorrência: converta para domínio antes de
     * serializar se a projeção importar.
     */
    @Mapping(target = "proximasExecucoes", ignore = true)
    public abstract LembreteOutputDTO toOutputDto( LembreteEntity entity );

    public abstract LembreteEntity toEntity( Lembrete lembrete );

    public abstract LembreteEntity updateEntity( LembreteInputDTO inputDTO, @MappingTarget LembreteEntity entity );

    /**
     * Garante a back-reference do lado dono da FK: sem isto, ao salvar o LembreteEntity por cascata,
     * notificacao/recorrencia ficariam com lembrete_id nulo.
     */
    @AfterMapping
    protected void vincularFilhos( @MappingTarget LembreteEntity entity )
    {
        NotificacaoEntity notificacao = entity.getNotificacao( );
        if( notificacao != null )
            notificacao.setLembrete( entity );

        RecorrenciaEntity recorrencia = entity.getRecorrencia( );
        if( recorrencia != null )
            recorrencia.setLembrete( entity );
    }
}
