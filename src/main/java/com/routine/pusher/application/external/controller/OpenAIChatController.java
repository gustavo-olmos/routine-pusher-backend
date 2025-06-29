package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.usecase.ChatUseCase;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/open-ai")
@Tag(name = "Open-AI", description = "Operações de chat relacionadas à lembretes")
public class OpenAIChatController
{
    private ChatUseCase useCase;


    @PostMapping
    public ResponseEntity<LembreteOutputDTO> criarLembreteViaChat( @RequestBody String frase )
    {
        return ResponseEntity.ok( ).body( useCase.criarLembreteViaChat( frase ) );
    }
}
