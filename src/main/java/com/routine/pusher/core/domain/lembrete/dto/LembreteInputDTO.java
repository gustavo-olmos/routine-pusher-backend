package com.routine.pusher.core.domain.lembrete.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.routine.pusher.core.domain.notificacao.dto.NotificacaoInputDTO;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaInputDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * As {@code @JsonPropertyDescription} abaixo não são decoração: elas entram no JSON Schema que a
 * integração de IA deriva desta classe e viram a documentação que o modelo lê para preencher cada
 * campo. Documentar aqui é documentar o contrato nas duas frentes — Swagger e IA — sem duplicar.
 */
public record LembreteInputDTO(
        @NotBlank(message = "O título do lembrete é obrigatório")
        // 255 é a largura da coluna. Sem este teto, texto longo — de um humano ou de um modelo que
        // se empolga — só falhava lá no INSERT, virando conflito de dados em vez de erro de campo.
        @Size(max = 255, message = "O título deve ter no máximo 255 caracteres")
        @JsonPropertyDescription("Título curto do lembrete, na voz do usuário: no máximo uma linha, "
                + "idealmente até 60 caracteres, começando com letra maiúscula")
        String titulo,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        @JsonPropertyDescription("Detalhe opcional do lembrete, no máximo uma frase")
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
