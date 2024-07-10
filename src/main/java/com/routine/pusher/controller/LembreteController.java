package com.routine.pusher.controller;

import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.service.interfaces.LembreteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/lembretes")
public class LembreteController
{
    private final Logger LOGGER = LoggerFactory.getLogger( LembreteController.class );

    private LembreteService service;

    public LembreteController(LembreteService service )
    {
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<LembreteDTO> adicionar( @RequestBody LembreteDTO dto )
    {
        LOGGER.debug("Adicionando lembrete");

        return ResponseEntity.ok( ).body( service.adicionar( dto ) );
    }

    @GetMapping
    public ResponseEntity<List<LembreteDTO>> listar( @RequestParam("sortInfo") String atributo,
                                                     @RequestParam("decrescente") boolean ordemReversa )
    {
        LOGGER.debug("Listando lembrete por: {}", atributo);

        return ResponseEntity.ok( ).body( service.listar( atributo, ordemReversa ) );
    }

    @PutMapping(path = "{id}")
    public ResponseEntity<LembreteDTO> editar( @PathVariable(value = "id") Long id,
                                               @RequestBody LembreteDTO dto )
    {
        LOGGER.debug("Alterando lembrete");

        return ResponseEntity.ok( ).body( service.editar( id, dto ) );
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<String> excluir( @PathVariable(value = "id") Long id )
    {
        LOGGER.debug("Excluindo lembrete");

        if ( service.excluir( id ) )
            return ResponseEntity.ok( "Lembrete excluído com sucesso!" );

        return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Lembrete não encontrada" );
    }
}
