package com.routine.pusher.controller;

import com.routine.pusher.model.dto.CategoriaDTO;
import com.routine.pusher.service.interfaces.CategoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/categorias")
public class CategoriaController
{
    private final Logger LOGGER = LoggerFactory.getLogger( CategoriaController.class );

    private CategoriaService service;

    public CategoriaController(CategoriaService service )
    {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> adicionar( @RequestBody CategoriaDTO dto )
    {
        LOGGER.debug("Adicionando categoria");

        return ResponseEntity.ok( ).body( service.adicionar( dto ) );
    }

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listar( @RequestParam("sortInfo") String atributo,
                                                     @RequestParam("decrescente") boolean ordemReversa )
    {
        LOGGER.debug("Listando categoria por: {}", atributo);

        return ResponseEntity.ok( ).body( service.listar( atributo, ordemReversa ) );
    }

    @PutMapping(path = "{id}")
    public ResponseEntity<CategoriaDTO> atualizar( @PathVariable(value = "id") Long id,
                                             @RequestBody CategoriaDTO dto )
    {
        LOGGER.debug("Alterando categoria");

        return ResponseEntity.ok( ).body( service.editar( id, dto ) );
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<String> excluir( @PathVariable(value = "id") Long id )
    {
        LOGGER.debug("Excluindo categoria");

        if ( service.excluir( id ) )
            return ResponseEntity.ok( "Categoria excluída com sucesso!" );

        return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Categoria não encontrada" );
    }
}
