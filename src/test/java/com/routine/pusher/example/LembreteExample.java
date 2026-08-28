package com.routine.pusher.example;

import com.routine.pusher.core.domain.categoria.CategoriaEntity;
import com.routine.pusher.core.domain.categoria.dto.CategoriaOutputDTO;
import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.notificacao.dto.NotificacaoInputDTO;
import com.routine.pusher.core.domain.notificacao.dto.NotificacaoOutputDTO;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaInputDTO;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaOutputDTO;
import com.routine.pusher.core.enums.EnumDiasDaSemana;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public abstract class LembreteExample {

    private LembreteExample() {}

    public static LembreteInputDTO simplesInput( )
    {
        RecorrenciaInputDTO recorrencia = new RecorrenciaInputDTO( 0, 0,
                9, 30, 0, List.of( ),
                List.of( EnumDiasDaSemana.SEGUNDA, EnumDiasDaSemana.QUARTA, EnumDiasDaSemana.SEXTA ) , null );

        NotificacaoInputDTO notificacao = new NotificacaoInputDTO(
                List.of( "VIBRAÇÃO" ), LocalTime.of( 12, 15 ),
                LocalDateTime.of( 2025, 9, 4, 12, 15 ),
                LocalDateTime.of( 2025, 9, 4, 12, 15 ), List.of( ) );

        return new LembreteInputDTO( "TESTE", "Lembrete teste", 1L,
                recorrencia, notificacao );
    }

    public static LembreteOutputDTO simplesOutput( )
    {
        CategoriaOutputDTO categoria = new CategoriaOutputDTO( 1L, "IMPORTANTE", "AMARELO", 1);

        RecorrenciaOutputDTO recorrencia = new RecorrenciaOutputDTO( 0, 0,
                9, 30, 0, List.of( ),
                List.of( EnumDiasDaSemana.SEGUNDA, EnumDiasDaSemana.QUARTA, EnumDiasDaSemana.SEXTA ) , null );

        NotificacaoOutputDTO notificacao = new NotificacaoOutputDTO(
                1L, List.of( "VIBRAÇÃO" ), LocalTime.of( 16, 20 ),
                LocalDateTime.of( 2025, 9, 4, 16, 20 ),
                LocalDateTime.of( 2025, 9, 16, 16, 20 ),
                LocalDateTime.of( 2025, 9, 4, 12, 15 ),
                LocalDateTime.of( 2025, 9, 16, 12, 15 ), List.of( ) );

        return new LembreteOutputDTO( UUID.randomUUID( ), "Lembrete teste", "Descrição teste",
                "PENDENTE", categoria, recorrencia, notificacao,
                List.of( LocalDateTime.of( 2025, 9, 5, 16, 20 ),
                         LocalDateTime.of( 2025, 9, 8, 16, 20 ) ) );
    }

    public static LembreteEntity entity( )
    {
        LembreteEntity entity = new LembreteEntity( );
        entity.setId( 1L );
        entity.setUuid( UUID.randomUUID( ) );
        entity.setTitulo( "Lembrete Teste" );
        entity.setDescricao( "Descrição teste" );
        entity.setStatus( "PENDENTE" );

        CategoriaEntity categoria = new CategoriaEntity( );
        categoria.setId( 1L );
        categoria.setNome( "Importante" );
        categoria.setCor( "Amarelo" );
        categoria.setFatorOrdem( 1 );
        entity.setCategoria( categoria );

        return entity;
    }
}
