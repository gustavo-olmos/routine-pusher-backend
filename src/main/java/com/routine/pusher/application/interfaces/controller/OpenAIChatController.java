package com.routine.pusher.application.interfaces.controller;

import com.routine.pusher.application.service.OpenAIChatService;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/open-ai")
public class OpenAIChatController
{
    private OpenAIChatService service;


    @PostMapping
    public ResponseEntity<LembreteOutputDTO> criarLembreteViaChat( @RequestBody String frase )
    {
        return ResponseEntity.ok( ).body( service.criarLembreteViaChat( frase ) );
    }
}
