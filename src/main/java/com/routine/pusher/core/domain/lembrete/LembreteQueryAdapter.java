package com.routine.pusher.core.domain.lembrete;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LembreteQueryAdapter implements LembreteQueryPort
{
    private final LembreteRepository lembreteRepository;


    @Override
    public boolean existeLembreteComCategoriaId( Long idCategoria )
    {
        return lembreteRepository.existsByCategoria_Id( idCategoria );
    }
}