package com.routine.pusher.core.domain.lembrete.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entrada da edição que <b>não</b> mexe no agendamento: só os campos descritivos do lembrete.
 *
 * <p>Existe separada de {@code LembreteInputDTO} porque o {@code PUT} reagenda o disparo sempre —
 * cancela e recria o trigger, recalcula a próxima execução e reabre o lembrete concluído. Isso é
 * certo quando o usuário mexeu na recorrência, e é dano colateral quando ele só corrigiu o título.
 * Um DTO que não carrega recorrência nem notificação torna impossível a rota de detalhes tocar no
 * que ela não deve.</p>
 */
public record LembreteDetalhesInputDTO(
        @NotBlank(message = "O título do lembrete é obrigatório")
        @Size(max = 255, message = "O título deve ter no máximo 255 caracteres")
        @Schema(description = "Título do lembrete", example = "Pagar a fatura do cartão")
        String titulo,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        @Schema(description = "Detalhe opcional do lembrete")
        String descricao,

        @NotNull(message = "A categoria do lembrete é obrigatória")
        @Schema(description = "Id de uma das categorias existentes", example = "1")
        Long categoriaId
) { }
