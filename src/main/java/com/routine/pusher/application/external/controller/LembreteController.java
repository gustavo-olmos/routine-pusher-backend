package com.routine.pusher.application.external.controller;

import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.application.service.interfaces.LembreteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/lembrete")
@Tag(name = "Lembrete", description = "Operações CRUD relacionadas à lembretes")
public class LembreteController
{
    private final LembreteService service;


    @PostMapping
    @Operation(summary = "Adiciona lembrete")
    public ResponseEntity<LembreteOutputDTO> salvar( @RequestBody LembreteInputDTO dto )
    {
        return ResponseEntity.ok( ).body( service.salvar( dto ) );
    }

    @GetMapping
    @Operation(summary = "Lista lembrete")
    public ResponseEntity<List<LembreteOutputDTO>> listar( @RequestParam("sortInfo") String atributo,
                                                           @RequestParam("decrescente") boolean ordemReversa )
    {
        return ResponseEntity.ok( ).body( service.listar( atributo, ordemReversa ) );
    }

    @PutMapping(path = "{id}")
    @Operation(summary = "Atualiza lembrete")
    public ResponseEntity<LembreteOutputDTO> atualizar( @PathVariable(value = "id") Long id,
                                                        @RequestBody LembreteInputDTO dto )
    {
        return ResponseEntity.ok( ).body( service.atualizar( id, dto ) );
    }


    @PatchMapping(path = "{id}")
    @Operation(summary = "Conclui lembrete")
    public ResponseEntity<Void> concluir( @PathVariable(value = "id") Long id )
    {
        service.concluir( id );
        return ResponseEntity.ok( ).build( );
    }

    @DeleteMapping(path = "{id}")
    @Operation(summary = "Exclui lembrete")
    public ResponseEntity<String> excluir( @PathVariable(value = "id") Long id )
    {
        return ( service.excluir( id ) )
                ? ResponseEntity.ok( "Lembrete excluído com sucesso!" )
                : ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Lembrete não encontrado" );
    }
}
