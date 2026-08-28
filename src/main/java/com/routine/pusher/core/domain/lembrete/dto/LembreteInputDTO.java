package com.routine.pusher.core.domain.lembrete.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.routine.pusher.core.domain.notificacao.dto.NotificacaoInputDTO;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaInputDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * As {@code @JsonPropertyDescription} abaixo não são decoração: elas entram no JSON Schema que a
 * integração de IA deriva desta classe e viram a documentação que o modelo lê para preencher cada
 * campo. Documentar aqui é documentar o contrato nas duas frentes — Swagger e IA — sem duplicar.
 */
public record LembreteInputDTO(
        @NotBlank(message = "O título do lembrete é obrigatório")
        @JsonPropertyDescription("Título curto do lembrete, na voz do usuário")
        String titulo,

        @JsonPropertyDescription("Detalhe opcional do lembrete")
        String descricao,

        @NotNull(message = "A categoria do lembrete é obrigatória")
        @JsonPropertyDescription("Id numérico de uma das categorias existentes, listadas no contexto")
        Long categoriaId,

        @Valid
        @JsonPropertyDescription("Como o lembrete se repete; omitir para lembrete de disparo único")
        RecorrenciaInputDTO recorrencia,

        @NotNull(message = "A notificação do lembrete é obrigatória")
        @Valid
        @JsonPropertyDescription("Quando e como notificar; obrigatório")
        NotificacaoInputDTO notificacao
) { }
