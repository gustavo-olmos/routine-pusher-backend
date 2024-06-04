package com.routine.pusher.service.interfaces;

import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.util.SortInfo;

import java.util.List;

public interface LembreteService
{
    List<LembreteDTO> listar( String atributo, boolean ordemReversa );

    LembreteDTO adicionar( LembreteDTO lembrete );

    LembreteDTO editar( Long id, LembreteDTO lembrete );

    boolean excluir( Long id );
}
