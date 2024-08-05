package com.routine.pusher.service.interfaces;

import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.util.SortInfo;

import java.util.List;

public interface LembreteService
{
    LembreteDTO adicionar( LembreteDTO lembrete );

    List<LembreteDTO> listar( String atributo, boolean ordemReversa );

    LembreteDTO atualizar( Long id, LembreteDTO lembrete );

    boolean excluir( Long id );
}
