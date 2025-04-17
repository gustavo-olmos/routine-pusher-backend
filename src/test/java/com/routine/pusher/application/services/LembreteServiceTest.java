package com.routine.pusher.application.services;

import com.routine.pusher.application.job.AgendadorJob;
import com.routine.pusher.application.service.LembreteServiceImpl;
import com.routine.pusher.data.example.input.LembreteInputDTOExample;
import com.routine.pusher.data.example.output.LembreteOutputDTOExample;
import com.routine.pusher.data.mapper.LembreteMapper;
import com.routine.pusher.data.model.dto.LembreteInputDTO;
import com.routine.pusher.data.model.dto.LembreteOutputDTO;
import com.routine.pusher.data.repository.LembreteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class LembreteServiceTest
{
    @Mock
    AgendadorJob agendador;

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

        doReturn( esperado ).when( agendador );
        AgendadorJob.agendar( esperado );

        LembreteOutputDTO output = new LembreteServiceImpl( mapper, repository ).salvar( input );

        assertThat( output ).isEqualTo( esperado );
    }
}
