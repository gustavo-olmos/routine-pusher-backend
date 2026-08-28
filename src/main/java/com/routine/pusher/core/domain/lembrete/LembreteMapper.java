package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.core.domain.feriado.port.FeriadoPort;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.notificacao.NotificacaoEntity;
import com.routine.pusher.core.domain.recorrencia.RecorrenciaEntity;
import com.routine.pusher.core.domain.sessao.SessaoAnonimaRepository;
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

    /** Resolve o UUID de sessão do domínio na FK interna da entidade — ver {@link #resolverSessao}. */
    @Inject
    protected SessaoAnonimaRepository sessaoRepository;

    @Mapping(target = "categoria.id", source = "categoriaId")
    public abstract Lembrete toDomain( LembreteInputDTO inputDto );

    @Mapping(target = "sessaoUuid", source = "sessao.uuid")
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

    @Mapping(target = "sessao", ignore = true)
    public abstract LembreteEntity toEntity( Lembrete lembrete );

    public abstract LembreteEntity updateEntity( LembreteInputDTO inputDTO, @MappingTarget LembreteEntity entity );

    /**
     * A resolução sessão-UUID → FK mora aqui, e não nos chamadores, porque são seis os pontos que
     * salvam entidade reconstruída do domínio — qualquer um que esquecesse de setar a sessão zeraria
     * a posse silenciosamente. Com a resolução no mapeamento, esquecer deixa de ser possível.
     *
     * <p>Declarar {@code Lembrete} como origem restringe o gancho ao {@link #toEntity(Lembrete)}:
     * no {@code updateEntity} a sessão da entidade carregada já está certa e não deve ser tocada.</p>
     */
    @AfterMapping
    protected void resolverSessao( Lembrete lembrete, @MappingTarget LembreteEntity entity )
    {
        if( sessaoRepository == null || lembrete.getSessaoUuid( ) == null ) return;

        sessaoRepository.findByUuid( lembrete.getSessaoUuid( ) ).ifPresent( entity::setSessao );
    }

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
