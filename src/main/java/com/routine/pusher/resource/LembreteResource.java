package com.routine.pusher.resource;

import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.service.interfaces.LembreteService;
import com.routine.pusher.util.SortInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/lembretes")
public class LembreteResource
{
    private LembreteService service;

    public LembreteResource( LembreteService service ) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<LembreteDTO>> getLembretesSortedBy(SortInfo sortInfo ) {
        return ResponseEntity.ok( ).body( service.readLembretesSortedBy( sortInfo ) );
    }

    @PostMapping
    public ResponseEntity postLembrete( LembreteDTO lembrete ) {
        return ResponseEntity.ok( ).body( service.createLembrete( lembrete ) );
    }

    @PutMapping
    public ResponseEntity putLembrete( LembreteDTO lembrete ) {
        return ResponseEntity.ok( ).body( service.updateLembrete( lembrete ) );
    }

    @DeleteMapping
    public ResponseEntity deleteLembrete( Long id ) {
        service.deleteLembrete( id );

        return ResponseEntity.ok("Lembrete Deletado com Sucesso!");
    }
}
