package com.routine.pusher.application.services;

import com.routine.pusher.application.service.CategoriaService;
import com.routine.pusher.core.domain.categoria.CategoriaEntity;
import com.routine.pusher.core.domain.categoria.CategoriaMapperImpl;
import com.routine.pusher.core.domain.categoria.CategoriaRepository;
import com.routine.pusher.core.domain.categoria.dto.CategoriaInputDTO;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import com.routine.pusher.core.domain.lembrete.LembreteQueryPort;
import com.routine.pusher.example.CategoriaExample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest
{
    @InjectMocks
    private CategoriaService service;

    @Mock
    private CategoriaMapperImpl mapper;

    @Mock
    private CategoriaRepository repository;

    @Mock
    private LembreteQueryPort lembreteQueryPort;


    @Test
    @DisplayName("Teste de sucesso para o método adicionar")
    void testSalvar_01( )
    {
        CategoriaInputDTO inputDTO = CategoriaExample.inputDTO( );
        CategoriaOutputDTO outputDTO = CategoriaExample.outputDTO( );
        CategoriaEntity entity = CategoriaExample.entity( );

        // 1. Arrange
        when( mapper.toEntity( inputDTO ) ).thenReturn( entity );
        when( repository.save( entity ) ).thenReturn( entity );
        when( mapper.toOutputDto( entity ) ).thenReturn( outputDTO );

        // 2. Act
        CategoriaOutputDTO resultado = service.adicionar(inputDTO);

        // 3. Assert
        assertThat( resultado ).isEqualTo( outputDTO );

        InOrder inOrder = inOrder( mapper, repository );
        inOrder.verify( mapper ).toEntity( inputDTO );
        inOrder.verify( repository ).save( entity );
        inOrder.verify( mapper ).toOutputDto( entity );
    }
}
