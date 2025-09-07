package com.routine.pusher.application.services;

import com.routine.pusher.application.service.LembreteService;
import com.routine.pusher.core.domain.categoria.port.CategoriaQueryPort;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import com.routine.pusher.core.domain.lembrete.LembreteMapperImpl;
import com.routine.pusher.core.domain.lembrete.LembreteRepository;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.example.LembreteExample;
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
class LembreteServiceTest
{
    @InjectMocks
    private LembreteService service;

    @Mock
    private LembreteMapperImpl mapper;

    @Mock
    private LembreteRepository repository;

    @Mock
    private CategoriaQueryPort categoriaQueryPort;


    @Test
    @DisplayName("Teste de sucesso para o método adicionar")
    void test_Salvar_01( )
    {
//        LembreteInputDTO inputDTO = LembreteExample.simplesInput( );
//        LembreteOutputDTO outputDTO = LembreteExample.simplesOutput( );
//        LembreteEntity entity = LembreteExample.entity( );
//        Lembrete domain = new Lembrete( );
//
//        // 1. Arrange
//        when( mapper.toDomain( inputDTO ) ).thenReturn( domain );
//        when( mapper.toEntity( domain ) ).thenReturn( entity );
//        when( repository.save( entity ) ).thenReturn( entity );
//        when( mapper.toOutputDto( entity ) ).thenReturn( outputDTO );
//
//        // 2. Act
//        LembreteOutputDTO resultado = service.adicionar(inputDTO);
//
//        // 3. Assert
//        assertThat( resultado ).isEqualTo( outputDTO );
//
//        InOrder inOrder = inOrder( mapper, repository );
//        inOrder.verify( mapper ).toDomain( inputDTO );
//        inOrder.verify( mapper ).toEntity( domain );
//        inOrder.verify( repository ).save( entity );
//        inOrder.verify( mapper ).toOutputDto( entity );
    }
}
