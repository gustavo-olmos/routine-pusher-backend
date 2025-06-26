package com.routine.pusher.core.domain.lembrete.factory;

import com.routine.pusher.application.service.interfaces.CategoriaService;
import com.routine.pusher.core.domain.categoria.Categoria;
import com.routine.pusher.core.domain.categoria.CategoriaMapper;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LembreteFactory
{
    private final CategoriaMapper categoriaMapper;
    private final CategoriaService categoriaService;

    public Lembrete criarLembrete( Lembrete lembrete )
    {
        CategoriaOutputDTO categoriaOutputDTO = categoriaService.buscarPeloId( lembrete.getCategoria( ).getId( ) );
        Categoria categoria = categoriaMapper.toDomain( categoriaOutputDTO );

        lembrete.setCategoria( categoria );
        return lembrete;
    }
}
