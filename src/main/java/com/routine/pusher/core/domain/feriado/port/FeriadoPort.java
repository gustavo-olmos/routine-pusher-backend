package com.routine.pusher.core.domain.feriado.port;

import java.time.LocalDate;

/**
 * Porta de consulta ao calendário de feriados.
 *
 * <p>Existe porque a origem do feriado é a parte instável do problema: hoje é cálculo em memória
 * (calendário nacional), amanhã pode ser API ou tabela para cobrir feriado estadual e municipal.
 * O motor de recorrência não pode saber a diferença — nem pagar I/O no meio do cálculo.</p>
 *
 * <p>Contrato: <b>rápido e sem exceção</b>. Esta consulta roda dentro da projeção, que por sua vez
 * roda a cada serialização de saída. Uma implementação que faça rede aqui precisa de cache próprio,
 * e uma falha de origem deve responder "não é feriado" — notificação a mais incomoda, notificação a
 * menos quebra o produto.</p>
 */
public interface FeriadoPort
{
    boolean ehFeriado( LocalDate data );
}
