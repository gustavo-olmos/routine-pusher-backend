package com.routine.pusher.application.usecase;

/**
 * Edição que preserva o agendamento: altera os campos descritivos do lembrete sem cancelar nem
 * recriar o disparo, e sem mexer no status.
 */
public interface AtualizarDetalhesUseCase<I, O, ID>
{
    O atualizarDetalhes( ID id, I inputDto );
}
