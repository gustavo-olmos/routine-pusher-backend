package com.routine.pusher.application.services;

import com.routine.pusher.application.service.LembreteServiceImpl;
import com.routine.pusher.core.domain.lembrete.LembreteMapper;
import com.routine.pusher.core.example.input.LembreteInputDTOExample;
import com.routine.pusher.core.example.output.LembreteOutputDTOExample;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import com.routine.pusher.core.domain.lembrete.LembreteRepository;
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

    @Spy
    LembreteMapper mapper;


    @Test
    @DisplayName("Processar: Testa XXX de YYY com ZZZ")
    void testProcessar01( )
    {
        LembreteInputDTO input = LembreteInputDTOExample.simples( );
        LembreteOutputDTO esperado = LembreteOutputDTOExample.simples( );
        Lembrete lembrete = mapper.toDomain( input );

        doReturn( esperado ).when( repository ).save( any( LembreteEntity.class ) );

        LembreteOutputDTO output = new LembreteServiceImpl( mapper, repository  ).salvar( input );

        verify( lembrete ).agendarLembrete( );
        verify( repository, times( 1 ) ).save( mapper.toEntity( lembrete ) );
        assertThat( output ).isEqualTo( esperado );
    }
}
