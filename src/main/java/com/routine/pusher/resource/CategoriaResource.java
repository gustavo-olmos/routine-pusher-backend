package com.routine.pusher.resource;

import com.routine.pusher.model.Categoria;
import com.routine.pusher.service.interfaces.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@Controller
public class CategoriaResource
{
    private CategoriaService service;

    public CategoriaResource( CategoriaService service ) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> getAl( ) {
        return ResponseEntity.ok( ).body( service.listar( ) );
    }

    @PostMapping
    public ResponseEntity post( String nome, String cor ) {
        return ResponseEntity.ok( ).body( service.adicionar( nome, cor ) );
    }

    @PutMapping
    public ResponseEntity put( Categoria categoria, Long id ) {
        return ResponseEntity.ok( ).body( service.editar( categoria,  id ) );
    }

    @DeleteMapping
    public ResponseEntity delete( Long id ) {
        service.excluir( id );

        return ResponseEntity.ok("Categoria excluída com sucesso!");
    }
}
