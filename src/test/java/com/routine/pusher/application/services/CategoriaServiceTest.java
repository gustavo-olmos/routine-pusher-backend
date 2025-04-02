package com.routine.pusher.application.services;

import com.routine.pusher.application.service.CategoriaServiceImpl;
import com.routine.pusher.data.mapper.CategoriaMapper;
import com.routine.pusher.data.model.dto.CategoriaInputDTO;
import com.routine.pusher.data.model.dto.CategoriaOutputDTO;
import com.routine.pusher.data.mothers.CategoriaInputDTOMother;
import com.routine.pusher.data.mothers.CategoriaOutputDTOMother;
import com.routine.pusher.data.repository.CategoriaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest
{
    @Mock
    CategoriaRepository repository;

    @Mock
    CategoriaMapper mapper;

    @Spy
    CategoriaServiceImpl categoriaService;

    @Test
    @DisplayName("Processar: Testa XXX de YYY com ZZZ")
    void testProcessar01( )
    {
        CategoriaInputDTO input = CategoriaInputDTOMother.simples( );
        CategoriaOutputDTO esperado = CategoriaOutputDTOMother.simples( );

        doReturn( esperado ).when( categoriaService ).adicionar( input );

        CategoriaOutputDTO output = new CategoriaServiceImpl( mapper, repository ).adicionar( input );

        assertThat( output ).isEqualTo( esperado );
    }
}
