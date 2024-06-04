package com.routine.pusher.resource;

import com.routine.pusher.model.dto.CategoriaDTO;
import com.routine.pusher.service.interfaces.CategoriaService;
import com.routine.pusher.util.SortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/categorias")
public class CategoriaResource
{
    private final Logger LOGGER = LoggerFactory.getLogger( CategoriaResource.class );

    private CategoriaService service;

    public CategoriaResource( CategoriaService service )
    {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> getAl( @RequestParam("sortInfo") String sortInfo,
                                                     @RequestParam("decrescente") boolean ordemReversa )
    {
        LOGGER.debug("Listando categoria por: {}", sortInfo);

        return ResponseEntity.ok( ).body( service.listar( sortInfo, ordemReversa ) );
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> post( @RequestBody CategoriaDTO dto )
    {
        LOGGER.debug("Adicionando categoria");

        return ResponseEntity.ok( ).body( service.adicionar( dto ) );
    }

    @PutMapping(path = "{id}")
    public ResponseEntity<CategoriaDTO> put( @PathVariable(value = "id") Long id,
                                             @RequestBody CategoriaDTO dto )
    {
        LOGGER.debug("Alterando categoria");

        return ResponseEntity.ok( ).body( service.editar( id, dto ) );
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<String> delete( @PathVariable(value = "id") Long id )
    {
        LOGGER.debug("Excluindo categoria");

        if ( service.excluir( id ) )
            return ResponseEntity.ok( "Categoria excluída com sucesso!" );

        return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Categoria não encontrada" );
    }
}
