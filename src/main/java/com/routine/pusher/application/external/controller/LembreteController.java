package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.application.usecase.ConcluirUseCase;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/lembrete")
@Tag(name = "Lembrete", description = "Operações CRUD relacionadas à lembretes")
public class LembreteController
{
    private final CRUDUseCase<LembreteInputDTO, LembreteOutputDTO> crudUseCase;
    private final ConcluirUseCase concluirUseCase;


    @PostMapping
    @Operation(summary = "Adiciona lembrete")
    public ResponseEntity<LembreteOutputDTO> salvar( @RequestBody LembreteInputDTO dto )
    {
        return ResponseEntity.ok( ).body( crudUseCase.adicionar( dto ) );
    }

    @GetMapping
    @Operation(summary = "Lista lembrete")
    public ResponseEntity<List<LembreteOutputDTO>> listar( @RequestParam("sortInfo") String atributo,
                                                           @RequestParam("decrescente") boolean ordemReversa )
    {
        return ResponseEntity.ok( ).body( crudUseCase.listar( atributo, ordemReversa ) );
    }

    @PutMapping(path = "{id}")
    @Operation(summary = "Atualiza lembrete")
    public ResponseEntity<LembreteOutputDTO> atualizar( @PathVariable(value = "id") Long id,
                                                        @RequestBody LembreteInputDTO dto )
    {
        return ResponseEntity.ok( ).body( crudUseCase.atualizar( id, dto ) );
    }


    @PatchMapping(path = "{id}")
    @Operation(summary = "Conclui lembrete")
    public ResponseEntity<Void> concluir( @PathVariable(value = "id") Long id )
    {
        concluirUseCase.concluir( id );
        return ResponseEntity.ok( ).build( );
    }

    @DeleteMapping(path = "{id}")
    @Operation(summary = "Exclui lembrete")
    public ResponseEntity<String> excluir( @PathVariable(value = "id") Long id )
    {
        crudUseCase.excluir( id );
        return ResponseEntity.ok( "Lembrete excluído com sucesso!" );
    }
}
