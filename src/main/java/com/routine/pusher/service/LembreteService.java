package com.routine.pusher.service;

import com.routine.pusher.model.Lembrete;
import com.routine.pusher.util.SortInfo;

import java.util.List;

public interface LembreteService
{
    Lembrete createLembrete( Lembrete lembrete );

    List<Lembrete> readLembretesSortedBy( SortInfo sortInfo );

    Lembrete updateLembrete( Lembrete lembrete );

    void deleteLembrete( Long id );
}
