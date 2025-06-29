package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.application.usecase.CRUDUseCase;
import com.routine.pusher.core.domain.categoria.Categoria;
import com.routine.pusher.core.domain.categoria.CategoriaMapper;
import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LembreteFactory
{
    private final LembreteMapper lembreteMapper;
    private final CategoriaMapper categoriaMapper;
    private final CRUDUseCase<CategoriaInputDTO, CategoriaOutputDTO> categoriaUseCase;

    public Lembrete construirLembrete( LembreteInputDTO inputDto )
    {
        CategoriaOutputDTO categoriaOutputDTO = categoriaUseCase.buscarPeloId( inputDto.categoriaId( ) );
        Categoria categoria = categoriaMapper.toDomain( categoriaOutputDTO );

        Lembrete lembrete = lembreteMapper.toDomain( inputDto );
        lembrete.setCategoria( categoria );
        lembrete.setExecucao( );

        return lembrete;
    }
}
