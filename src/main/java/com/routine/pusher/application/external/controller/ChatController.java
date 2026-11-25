package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.usecase.ChatUseCase;
import com.routine.pusher.application.usecase.InterpretacaoUseCase;
import com.routine.pusher.core.domain.lembrete.dto.InterpretacaoLembreteOutputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteChatInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rota neutra de propósito ({@code /chat}, não {@code /open-ai}): o provedor do modelo é detalhe de
 * implementação atrás do {@code ChatClient} e já trocou uma vez — a URL não deve trocar junto.
 */
@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/chat")
@Tag(name = "Chat IA", description = "Lembretes a partir de linguagem natural: interpretar, revisar e criar")
public class ChatController
{
    private final ChatUseCase<LembreteOutputDTO> useCase;
    private final InterpretacaoUseCase<InterpretacaoLembreteOutputDTO> interpretacaoUseCase;


    @PostMapping(path = "/interpretacao")
    @Operation(summary = "Interpreta uma frase e devolve o rascunho do lembrete, sem salvar",
               description = "Consome uma chamada da cota de IA da sessão. O campo 'lembrete' da "
                       + "resposta é o corpo do POST /api/v1/lembrete: confirmar é reenviá-lo, "
                       + "o que não consome cota de IA.")
    @ApiResponse(responseCode = "200", description = "Rascunho montado e validado, com a prévia das próximas execuções")
    @ApiResponse(responseCode = "422", description = "A IA montou algo inválido ou que nunca dispararia")
    @ApiResponse(responseCode = "429", description = "Cota de IA ou limite de lembretes da sessão atingido")
    public ResponseEntity<InterpretacaoLembreteOutputDTO> interpretarLembrete(
            @Valid @RequestBody LembreteChatInputDTO entrada )
    {
        return ResponseEntity.ok( interpretacaoUseCase.interpretarLembrete( entrada ) );
    }

    /**
     * @deprecated interpreta e salva sem revisão. Use {@code POST /api/v1/chat/interpretacao} e
     * confirme com {@code POST /api/v1/lembrete}. Removido quando o front publicado migrar.
     */
    @Deprecated
    @PostMapping(path = "/lembrete")
    @Operation(summary = "Cria um lembrete a partir de uma frase, sem revisão (obsoleto)",
               description = "Use POST /api/v1/chat/interpretacao para obter o rascunho e "
                       + "POST /api/v1/lembrete para confirmar.",
               deprecated = true)
    public ResponseEntity<LembreteOutputDTO> criarLembreteViaChat(
            @Valid @RequestBody LembreteChatInputDTO entrada )
    {
        return ResponseEntity.ok( ).body( useCase.criarLembreteViaChat( entrada ) );
    }
}
