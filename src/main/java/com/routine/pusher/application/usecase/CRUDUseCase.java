package com.routine.pusher.application.usecase;

import java.util.List;

public interface CRUDUseCase<I, O>
{
    O adicionar( I inputDTO );

    List<O> listar( String atributo, boolean ordemReversa );

    default O buscarPeloId( Long id ) {
        return null;
    };

    O atualizar( Long id, I inputDTO );

    void excluir( Long id );
}
