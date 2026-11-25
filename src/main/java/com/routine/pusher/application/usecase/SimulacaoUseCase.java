package com.routine.pusher.application.usecase;

/**
 * Responde "como ficaria" sem criar nada: monta e valida o rascunho pelas mesmas regras da criação
 * e devolve a prévia. Existe para o chat revisar antes de salvar sem mexer em mapper ou repositório.
 *
 * @param <I> entrada do rascunho
 * @param <P> prévia calculada sobre ele
 */
public interface SimulacaoUseCase<I, P>
{
    /**
     * Falha se a sessão já não pode criar mais nada. Separado de {@link #simular} porque quem
     * simula pode querer checar isto <b>antes</b> de um passo caro, como a chamada de IA.
     */
    void validarVagaNaSessao( );

    /** Mesma montagem e mesmas recusas de uma criação, sem persistir nem agendar. */
    P simular( I rascunho );
}
