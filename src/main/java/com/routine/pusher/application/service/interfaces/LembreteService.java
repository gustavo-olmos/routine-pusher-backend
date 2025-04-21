package com.routine.pusher.application.service.interfaces;

import com.routine.pusher.data.model.dto.LembreteInputDTO;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;

import java.util.List;

public interface LembreteService
{
    LembreteOutputDTO salvar( LembreteInputDTO lembrete );

    void concluir( Long id );

    List<LembreteOutputDTO> listar( String atributo, boolean ordemReversa );

    boolean excluir( Long id );
}
