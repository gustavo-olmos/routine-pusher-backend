package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.usecase.ChatUseCase;
import com.routine.pusher.core.domain.lembrete.dto.LembreteChatInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Chat IA", description = "Criação de lembretes a partir de linguagem natural")
public class ChatController
{
    private final ChatUseCase<LembreteOutputDTO> useCase;


    @PostMapping(path = "/lembrete")
    @Operation(summary = "Cria um lembrete a partir de uma frase em linguagem natural")
    public ResponseEntity<LembreteOutputDTO> criarLembreteViaChat(
            @Valid @RequestBody LembreteChatInputDTO entrada )
    {
        return ResponseEntity.ok( ).body( useCase.criarLembreteViaChat( entrada ) );
    }
}
