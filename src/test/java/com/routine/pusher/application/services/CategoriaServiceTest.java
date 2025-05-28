package com.routine.pusher.application.services;

import com.routine.pusher.application.service.CategoriaServiceImpl;
import com.routine.pusher.core.mapper.CategoriaMapperImpl;
import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import com.routine.pusher.core.example.input.CategoriaInputDTOExample;
import com.routine.pusher.core.example.output.CategoriaOutputDTOExample;
import com.routine.pusher.core.domain.categoria.CategoriaEntity;
import com.routine.pusher.core.domain.categoria.CategoriaRepository;
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

    @Test
    @DisplayName("Processar: Testa XXX de YYY com ZZZ")
    void testProcessar01( )
    {
        CategoriaInputDTO input = CategoriaInputDTOExample.simples( );
        CategoriaOutputDTO esperado = CategoriaOutputDTOExample.simples( );

        doReturn( esperado ).when( repository ).save( any( CategoriaEntity.class ) );

        CategoriaOutputDTO output = new CategoriaServiceImpl( mapper, repository ).adicionar( input );

        assertThat( output ).isEqualTo( esperado );
        verify( repository, times( 1 ) ).save( mapper.toEntity( input ) );
    }
}
