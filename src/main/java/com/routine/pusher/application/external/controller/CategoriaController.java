package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/categoria")
@Tag(name = "Categoria", description = "Operações CRUD relacionadas à categoria de lembrete")
public class CategoriaController
{
    private final CRUDUseCase<CategoriaInputDTO, CategoriaOutputDTO> useCase;


    @PostMapping
    @Operation(summary = "Adiciona categoria")
    public ResponseEntity<CategoriaOutputDTO> adicionar( @RequestBody CategoriaInputDTO dto )
    {
        return ResponseEntity.ok( ).body( useCase.adicionar( dto ) );
    }

    @GetMapping
    @Operation(summary = "Lista categoria")
    public ResponseEntity<List<CategoriaOutputDTO>> listar( @RequestParam("sortInfo") String atributo,
                                                            @RequestParam("decrescente") boolean ordemReversa )
    {
        return ResponseEntity.ok( ).body( useCase.listar( atributo, ordemReversa ) );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza categoria")
    public ResponseEntity<CategoriaOutputDTO> atualizar( @PathVariable(value = "id") Long id,
                                                         @RequestBody CategoriaInputDTO dto )
    {
        return ResponseEntity.ok( ).body( useCase.atualizar( id, dto ) );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui categoria")
    public ResponseEntity<String> excluir( @PathVariable(value = "id") Long id )
    {
        useCase.excluir( id );
        return ResponseEntity.ok( "Categoria excluída com sucesso!" );
    }
}
