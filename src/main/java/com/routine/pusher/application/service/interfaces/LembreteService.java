package com.routine.pusher.application.service.interfaces;

import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;

import java.util.List;

public interface LembreteService
{
    LembreteOutputDTO salvar( LembreteInputDTO lembrete );

    LembreteOutputDTO atualizar( Long id, LembreteInputDTO inputDTO );

    void concluir( Long id );

    List<LembreteOutputDTO> listar( String atributo, boolean ordemReversa );

    boolean excluir( Long id );
}
