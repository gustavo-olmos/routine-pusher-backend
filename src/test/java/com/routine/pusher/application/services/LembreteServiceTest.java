package com.routine.pusher.application.services;

import com.routine.pusher.application.service.LembreteServiceImpl;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import com.routine.pusher.core.domain.lembrete.LembreteMapperImpl;
import com.routine.pusher.core.domain.lembrete.LembreteRepository;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.lembrete.factory.LembreteFactory;
import com.routine.pusher.core.example.input.LembreteInputDTOExample;
import com.routine.pusher.core.example.output.LembreteOutputDTOExample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LembreteServiceTest
{
    @Mock
    LembreteRepository repository;

    @Mock
    LembreteFactory factory;

    @Spy
    LembreteMapperImpl mapper;


    @Test
    @DisplayName("Processar: Testa XXX de YYY com ZZZ")
    void testProcessar01( )
    {
        LembreteInputDTO input = LembreteInputDTOExample.simples( );
        LembreteOutputDTO esperado = LembreteOutputDTOExample.simples( );

        doReturn( esperado ).when( repository ).save( any( LembreteEntity.class ) );

        LembreteOutputDTO output = new LembreteServiceImpl( mapper, factory, repository ).salvar( input );
        Lembrete lembrete = mapper.toDomain( input );

        LembreteEntity entidade = mapper.toEntity( lembrete );
        lembrete = mapper.toDomain( entidade );

        verify( lembrete ).agendarLembrete( );
        verify( repository, times( 1 ) ).save( mapper.toEntity( lembrete ) );
        assertThat( output ).isEqualTo( esperado );
    }
}
