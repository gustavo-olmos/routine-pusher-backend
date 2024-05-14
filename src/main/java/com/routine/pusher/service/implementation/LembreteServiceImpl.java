package com.routine.pusher.service.implementation;

import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.repository.LembreteRepository;
import com.routine.pusher.service.interfaces.LembreteService;
import com.routine.pusher.util.SortInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LembreteServiceImpl implements LembreteService
{
    private SortInfo sortInfo;
    private LembreteRepository repository;

    public LembreteServiceImpl( SortInfo sortInfo, LembreteRepository repository ) {
        this.sortInfo = sortInfo;
        this.repository = repository;
    }

    @Override
    public LembreteDTO createLembrete( LembreteDTO lembrete ) {
        return null;
    }

    @Override
    public List<LembreteDTO> readLembretesSortedBy( SortInfo sortInfo ) {
        return List.of();
    }

    @Override
    public LembreteDTO updateLembrete(LembreteDTO lembrete ) {
        return null;
    }

    @Override
    public void deleteLembrete( Long id ) { }
}
