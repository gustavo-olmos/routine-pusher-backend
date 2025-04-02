package com.routine.pusher.application.external.controller;

import com.routine.pusher.data.model.dto.CategoriaInputDTO;
import com.routine.pusher.data.model.dto.CategoriaOutputDTO;
import com.routine.pusher.application.service.interfaces.CategoriaService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/categorias")
public class CategoriaController
{
    private final CategoriaService service;


    @PostMapping
    public ResponseEntity<CategoriaOutputDTO> adicionar(@RequestBody CategoriaInputDTO dto )
    {
        return ResponseEntity.ok( ).body( service.adicionar( dto ) );
    }

    @GetMapping
    public ResponseEntity<List<CategoriaOutputDTO>> listar(@RequestParam("sortInfo") String atributo,
                                                           @RequestParam("decrescente") boolean ordemReversa )
    {
        return ResponseEntity.ok( ).body( service.listar( atributo, ordemReversa ) );
    }

    @PutMapping(path = "{id}")
    public ResponseEntity<CategoriaOutputDTO> atualizar(@PathVariable(value = "id") Long id,
                                                        @RequestBody CategoriaInputDTO dto )
    {
        return ResponseEntity.ok( ).body( service.atualizar( id, dto ) );
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<String> excluir( @PathVariable(value = "id") Long id )
    {
        return ( service.excluir( id ) )
            ? ResponseEntity.ok( "Categoria excluída com sucesso!" )
            : ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Categoria não encontrada" );
    }
}
