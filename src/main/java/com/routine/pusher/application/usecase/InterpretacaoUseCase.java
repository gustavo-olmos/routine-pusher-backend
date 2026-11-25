package com.routine.pusher.application.usecase;

import com.routine.pusher.core.domain.lembrete.dto.LembreteChatInputDTO;

/**
 * Transforma linguagem natural num rascunho revisável, sem salvar. A confirmação é responsabilidade
 * de quem recebe o rascunho, pelo mesmo caminho de uma criação comum.
 */
public interface InterpretacaoUseCase<T>
{
    T interpretarLembrete( LembreteChatInputDTO entrada );
}
