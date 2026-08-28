package com.routine.pusher.application.usecase;

import java.util.List;

/**
 * {@code ID} existe porque nem todo agregado se identifica do mesmo jeito: categoria ainda usa o id
 * sequencial, lembrete usa UUID. Sem o parâmetro, o contrato obrigaria os dois ao mesmo tipo.
 */
public interface CRUDUseCase<I, O, ID>
{
    O adicionar( I inputDTO );

    List<O> listar( String atributo, boolean ordemReversa );

    default O buscarPeloId( ID id ) {
        return null;
    };

    O atualizar( ID id, I inputDTO );

    void excluir( ID id );
}
