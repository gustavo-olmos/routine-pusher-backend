package com.routine.pusher.application.external.controller;

import com.routine.pusher.data.model.dto.CategoriaInputDTO;
import com.routine.pusher.data.model.dto.CategoriaOutputDTO;
import com.routine.pusher.application.service.interfaces.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/categoria")
@Tag(name = "Categoria", description = "Operações CRUD relacionadas à categoria de lembrete")
public class CategoriaController
{
    private final CategoriaService service;


    @PostMapping
    @Operation(summary = "Adiciona categoria")
    public ResponseEntity<CategoriaOutputDTO> adicionar( @RequestBody CategoriaInputDTO dto )
    {
        return ResponseEntity.ok( ).body( service.adicionar( dto ) );
    }

    @GetMapping
    @Operation(summary = "Lista categoria")
    public ResponseEntity<List<CategoriaOutputDTO>> listar( @RequestParam("sortInfo") String atributo,
                                                            @RequestParam("decrescente") boolean ordemReversa )
    {
        return ResponseEntity.ok( ).body( service.listar( atributo, ordemReversa ) );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza categoria")
    public ResponseEntity<CategoriaOutputDTO> atualizar( @PathVariable(value = "id") Long id,
                                                         @RequestBody CategoriaInputDTO dto )
    {
        return ResponseEntity.ok( ).body( service.atualizar( id, dto ) );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui categoria")
    public ResponseEntity<String> excluir( @PathVariable(value = "id") Long id )
    {
        return ( service.excluir( id ) )
            ? ResponseEntity.ok( "Categoria excluída com sucesso!" )
            : ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Categoria não encontrada" );
    }
}
