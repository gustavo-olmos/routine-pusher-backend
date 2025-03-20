package com.routine.pusher.application.interfaces.controller;

import com.routine.pusher.data.model.dto.LembreteInputDTO;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.application.service.LembreteService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/lembretes")
public class LembreteController
{
    private final LembreteService service;


    @PostMapping
    public ResponseEntity<LembreteOutputDTO> adicionar( @RequestBody LembreteInputDTO dto )
    {
        return ResponseEntity.ok( ).body( service.adicionar( dto ) );
    }

    @PostMapping
    public ResponseEntity<LembreteOutputDTO> adicionarViaIA( @RequestBody String frase )
    {
        return ResponseEntity.ok( ).body( service.adicionarViaIA( frase ) );
    }

    @GetMapping
    public ResponseEntity<List<LembreteOutputDTO>> listar( @RequestParam("sortInfo") String atributo,
                                                          @RequestParam("decrescente") boolean ordemReversa )
    {
        return ResponseEntity.ok( ).body( service.listar( atributo, ordemReversa ) );
    }

    @PutMapping(path = "{id}")
    public ResponseEntity<LembreteOutputDTO> atualizar( @PathVariable(value = "id") Long id,
                                                       @RequestBody LembreteInputDTO dto )
    {
        return ResponseEntity.ok( ).body( service.atualizar( id, dto ) );
    }

    @PatchMapping(path = "{id}")
    public ResponseEntity<String> concluirLembrete( @PathVariable(value = "id") Long id )
    {
        return ResponseEntity.ok( ).body( service.concluir( id ) );
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<String> excluir( @PathVariable(value = "id") Long id )
    {
        return ( service.excluir( id ) )
                ? ResponseEntity.ok( "Lembrete excluído com sucesso!" )
                : ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Lembrete não encontrada" );
    }
}
