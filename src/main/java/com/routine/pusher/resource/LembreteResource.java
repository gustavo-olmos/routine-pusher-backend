package com.routine.pusher.resource;

import com.routine.pusher.model.Lembrete;
import com.routine.pusher.service.LembreteService;
import com.routine.pusher.util.SortInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@Controller
public class LembreteResource
{
    private LembreteService service;

    public LembreteResource( LembreteService service )
    {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Lembrete>> getLembretesSortedBy(SortInfo sortInfo )
    {
        return ResponseEntity.ok( ).body( service.readLembretesSortedBy( sortInfo ) );
    }

    @PostMapping
    public ResponseEntity postLembrete( Lembrete lembrete )
    {
        return ResponseEntity.ok( ).body( service.createLembrete( lembrete ) );
    }

    @PutMapping
    public ResponseEntity putLembrete( Lembrete lembrete )
    {
        return ResponseEntity.ok( ).body( service.updateLembrete( lembrete ) );
    }
}
