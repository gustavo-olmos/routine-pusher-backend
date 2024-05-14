package com.routine.pusher.service.interfaces;

import com.routine.pusher.model.dto.LembreteDTO;
import com.routine.pusher.util.SortInfo;

import java.util.List;

public interface LembreteService
{
    LembreteDTO createLembrete(LembreteDTO lembrete );

    List<LembreteDTO> readLembretesSortedBy(SortInfo sortInfo );

    LembreteDTO updateLembrete(LembreteDTO lembrete );

    void deleteLembrete( Long id );
}
