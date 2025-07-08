package com.routine.pusher.application.services;

import com.routine.pusher.application.service.CategoriaService;
import com.routine.pusher.core.domain.lembrete.LembreteQueryPort;
import com.routine.pusher.core.domain.categoria.CategoriaEntity;
import com.routine.pusher.core.domain.categoria.CategoriaMapperImpl;
import com.routine.pusher.core.domain.categoria.CategoriaRepository;
import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import com.routine.pusher.core.example.input.CategoriaInputDTOExample;
import com.routine.pusher.core.example.output.CategoriaOutputDTOExample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest
{
    @Mock
    CategoriaRepository repository;

    @Spy
    CategoriaMapperImpl mapper;

    @Mock
    LembreteQueryPort lembreteQueryPort;

    @Test
    @DisplayName("Processar: Testa XXX de YYY com ZZZ")
    void testProcessar01( )
    {
        CategoriaInputDTO input = CategoriaInputDTOExample.simples( );
        CategoriaOutputDTO esperado = CategoriaOutputDTOExample.simples( );

        doReturn( esperado ).when( repository ).save( any( CategoriaEntity.class ) );

        CategoriaOutputDTO output = new CategoriaService( mapper, repository, lembreteQueryPort ).adicionar( input );

        assertThat( output ).isEqualTo( esperado );
        verify( repository, times( 1 ) ).save( mapper.toEntity( input ) );
    }
}
