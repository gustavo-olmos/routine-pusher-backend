package com.routine.pusher.resource;

import com.routine.pusher.model.dto.CategoriaDTO;
import com.routine.pusher.service.interfaces.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/categorias")
public class CategoriaResource
{
    private CategoriaService service;

    public CategoriaResource( CategoriaService service ) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> getAl( ) {
        return ResponseEntity.ok( ).body( service.listar( ) );
    }

    @PostMapping
    public ResponseEntity post( String nome, String cor ) {
        return ResponseEntity.ok( ).body( service.adicionar( nome, cor ) );
    }

    @PutMapping
    public ResponseEntity put(CategoriaDTO categoria, Long id ) {
        return ResponseEntity.ok( ).body( service.editar( categoria,  id ) );
    }

    @DeleteMapping
    public ResponseEntity delete( Long id ) {
        service.excluir( id );

        return ResponseEntity.ok("Categoria excluída com sucesso!");
    }
}
