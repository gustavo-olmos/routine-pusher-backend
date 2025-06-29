package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.core.domain.categoria.Categoria;
import com.routine.pusher.core.domain.categoria.CategoriaMapper;
import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LembreteFactory
{
    private final CategoriaMapper categoriaMapper;
    private final CRUDUseCase<CategoriaInputDTO, CategoriaOutputDTO> categoriaUseCase;

    public void construirLembrete( Lembrete lembrete )
    {
        CategoriaOutputDTO categoriaOutputDTO = categoriaUseCase.buscarPeloId( lembrete.getCategoria( ).getId( ) );
        Categoria categoria = categoriaMapper.toDomain( categoriaOutputDTO );

        lembrete.setCategoria( categoria );
        lembrete.setExecucao( );

        lembrete.agendarLembrete( );
    }
}
