package com.routine.pusher.application.external.controller;

import com.routine.pusher.data.model.dto.LembreteInputDTO;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.application.service.interfaces.LembreteService;
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
    public ResponseEntity<LembreteOutputDTO> salvar( @RequestBody LembreteInputDTO dto )
    {
        return ResponseEntity.ok( ).body( service.salvar( dto ) );
    }

    @GetMapping
    public ResponseEntity<List<LembreteOutputDTO>> listar( @RequestParam("sortInfo") String atributo,
                                                           @RequestParam("decrescente") boolean ordemReversa )
    {
        return ResponseEntity.ok( ).body( service.listar( atributo, ordemReversa ) );
    }

    @PutMapping(path = "{id}")
    public ResponseEntity<Void> concluir( @PathVariable(value = "id") Long id )
    {
        service.concluir( id );
        return ResponseEntity.ok( ).build( );
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<String> excluir( @PathVariable(value = "id") Long id )
    {
        return ( service.excluir( id ) )
                ? ResponseEntity.ok( "Lembrete excluído com sucesso!" )
                : ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Lembrete não encontrado" );
    }
}
