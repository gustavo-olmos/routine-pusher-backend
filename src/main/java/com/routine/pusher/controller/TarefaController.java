package com.routine.pusher.controller;

import com.routine.pusher.model.dto.TarefaDTO;
import com.routine.pusher.service.interfaces.TarefaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/tarefas")
public class TarefaController
{
    private final Logger LOGGER = LoggerFactory.getLogger( TarefaController.class );

    private TarefaService service;

    public TarefaController( TarefaService service )
    {
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<TarefaDTO> adicionar( @RequestBody TarefaDTO dto )
    {
        LOGGER.debug("Adicionando tarefa");

        return ResponseEntity.ok( ).body( service.adicionar( dto ) );
    }

    @PutMapping
    public ResponseEntity<TarefaDTO> atualizar( @PathVariable(value = "id") Long id,
                                             @RequestBody TarefaDTO dto )
    {
        LOGGER.debug("Alterando tarefa");

        return ResponseEntity.ok( ).body( service.atualizar( id, dto ) );
    }

    @DeleteMapping
    public ResponseEntity<String> excluir( @PathVariable(value = "id") Long id )
    {
        LOGGER.debug("Excluindo tarefa");

        return ( service.excluir( id ) ) ? ResponseEntity.ok( "Tarefa excluída com sucesso!" ) :
                new ResponseEntity<>( "Tarefa não encontrada", HttpStatus.NOT_FOUND );
    }
}
