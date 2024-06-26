package com.routine.pusher.resource;

import com.routine.pusher.model.dto.TarefaDTO;
import com.routine.pusher.service.interfaces.TarefaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/tarefas")
public class TarefaResource
{
    private final Logger LOGGER = LoggerFactory.getLogger( TarefaResource.class );

    private TarefaService service;

    public TarefaResource( TarefaService service )
    {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<TarefaDTO>> listar( @RequestParam("sortInfo") String atributo,
                                                  @RequestParam("decrescente") boolean ordemReversa )
    {
        LOGGER.debug("Listando tarefa por: {}", atributo);

        return ResponseEntity.ok( ).body( service.listar( atributo, ordemReversa ) );
    }

    @PostMapping
    public ResponseEntity<TarefaDTO> adicionar( @RequestBody TarefaDTO dto )
    {
        LOGGER.debug("Adicionando tarefa");

        return ResponseEntity.ok( ).body( service.adicionar( dto ) );
    }

    @PutMapping
    public ResponseEntity<TarefaDTO> editar( @PathVariable(value = "id") Long id,
                                             @RequestBody TarefaDTO dto )
    {
        LOGGER.debug("Alterando tarefa");

        return ResponseEntity.ok( ).body( service.editar( id, dto ) );
    }

    @DeleteMapping
    public ResponseEntity excluir( @PathVariable(value = "id") Long id )
    {
        LOGGER.debug("Excluindo tarefa");

        if ( service.excluir( id ) )
            return ResponseEntity.ok( "Tarefa excluída com sucesso!" );

        return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( "Tarefa não encontrada" );
    }
}
