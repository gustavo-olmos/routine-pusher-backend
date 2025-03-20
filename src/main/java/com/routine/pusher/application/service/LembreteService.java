package com.routine.pusher.application.service;

import com.routine.pusher.data.model.dto.LembreteInputDTO;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;

import java.util.List;

public interface LembreteService
{
    LembreteOutputDTO adicionar( LembreteInputDTO lembrete );

    LembreteOutputDTO adicionarViaIA( String frase );

    List<LembreteOutputDTO> listar( String atributo, boolean ordemReversa );

    LembreteOutputDTO atualizar( Long id, LembreteInputDTO lembrete );

    String concluir( Long id );

    boolean excluir( Long id );
}
