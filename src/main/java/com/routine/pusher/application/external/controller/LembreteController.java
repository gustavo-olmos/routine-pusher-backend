package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.application.usecase.ConcluirUseCase;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/lembrete")
@Tag(name = "Lembrete", description = "Operações CRUD relacionadas à lembretes")
public class LembreteController
{
    private final CRUDUseCase<LembreteInputDTO, LembreteOutputDTO, UUID> crudUseCase;
    private final ConcluirUseCase<UUID> concluirUseCase;


    @PostMapping
    @Operation(summary = "Adiciona lembrete")
    public ResponseEntity<LembreteOutputDTO> salvar( @Valid @RequestBody LembreteInputDTO dto )
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

    @PutMapping(path = "/{uuid}")
    @Operation(summary = "Atualiza lembrete")
    public ResponseEntity<LembreteOutputDTO> atualizar( @PathVariable(value = "uuid") UUID uuid,
                                                        @Valid @RequestBody LembreteInputDTO dto )
    {
        return ResponseEntity.ok( ).body( crudUseCase.atualizar( uuid, dto ) );
    }


    @PatchMapping(path = "/{uuid}")
    @Operation(summary = "Conclui lembrete")
    public ResponseEntity<Void> concluir( @PathVariable(value = "uuid") UUID uuid )
    {
        concluirUseCase.concluir( uuid );
        return ResponseEntity.ok( ).build( );
    }

    @DeleteMapping(path = "/{uuid}")
    @Operation(summary = "Exclui lembrete")
    public ResponseEntity<String> excluir( @PathVariable(value = "uuid") UUID uuid )
    {
        crudUseCase.excluir( uuid );
        return ResponseEntity.ok( "Lembrete excluído com sucesso!" );
    }
}
