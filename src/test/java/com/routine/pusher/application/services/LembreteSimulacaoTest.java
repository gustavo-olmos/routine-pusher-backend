package com.routine.pusher.application.services;

import com.routine.pusher.application.job.AgendadorJob;
import com.routine.pusher.application.service.LembreteService;
import com.routine.pusher.core.domain.categoria.Categoria;
import com.routine.pusher.core.domain.categoria.port.CategoriaQueryPort;
import com.routine.pusher.core.domain.feriado.port.FeriadoPort;
import com.routine.pusher.core.domain.lembrete.Lembrete;
import com.routine.pusher.core.domain.lembrete.LembreteEntity;
import com.routine.pusher.core.domain.lembrete.LembreteMapperImpl;
import com.routine.pusher.core.domain.lembrete.LembreteRepository;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.lembrete.dto.LembreteOutputDTO;
import com.routine.pusher.core.domain.notificacao.dto.NotificacaoInputDTO;
import com.routine.pusher.core.domain.recorrencia.dto.RecorrenciaInputDTO;
import com.routine.pusher.core.domain.sessao.port.SessaoAtualPort;
import com.routine.pusher.core.enums.EnumDiasDaSemana;
import com.routine.pusher.infrastructure.exceptions.StrategyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * A simulação é a criação sem efeito: mesmas regras, nada persistido, nada agendado. O mapper e o
 * domínio são reais de propósito — a pergunta aqui é se a projeção funciona sobre um lembrete que
 * nunca passou pelo banco, e um mock responderia o que mandássemos.
 */
@ExtendWith(MockitoExtension.class)
class LembreteSimulacaoTest
{
    private static final UUID SESSAO = UUID.randomUUID( );

    @Mock
    private LembreteRepository repository;

    @Mock
    private CategoriaQueryPort categoriaQueryPort;

    @Mock
    private AgendadorJob agendadorJob;

    @Mock
    private FeriadoPort feriadoPort;

    @Mock
    private SessaoAtualPort sessaoAtual;

    private LembreteService service;

    @BeforeEach
    void montar( )
    {
        service = new LembreteService( new LembreteMapperImpl( ), repository, categoriaQueryPort,
                agendadorJob, feriadoPort, sessaoAtual );

        lenient( ).when( sessaoAtual.uuid( ) ).thenReturn( SESSAO );
        Categoria categoria = new Categoria( );
        categoria.setId( 1L );
        lenient( ).when( categoriaQueryPort.buscarPorId( 1L ) ).thenReturn( categoria );
    }

    private static LembreteInputDTO todaSegundaAs9( )
    {
        RecorrenciaInputDTO recorrencia = new RecorrenciaInputDTO( null, null, null, null, null,
                List.of( ), List.of( EnumDiasDaSemana.SEGUNDA ), null );
        NotificacaoInputDTO notificacao = new NotificacaoInputDTO( List.of( "pop-up" ),
                LocalTime.of( 9, 0 ), LocalDateTime.now( ), null, List.of( ) );

        return new LembreteInputDTO( "Revisar orçamento", null, 1L, recorrencia, notificacao );
    }

    @Test
    @DisplayName("lembrete sem id nem entidade salva projeta as próximas execuções normalmente")
    void simular_projetaSemPersistir( )
    {
        List<LocalDateTime> previa = service.simular( todaSegundaAs9( ) );

        assertThat( previa ).hasSize( Lembrete.LIMITE_PROXIMAS_EXECUCOES )
                .allMatch( d -> d.getDayOfWeek( ) == DayOfWeek.MONDAY && d.toLocalTime( ).equals( LocalTime.of( 9, 0 ) ) )
                .allMatch( d -> d.isAfter( LocalDateTime.now( ) ) )
                .isSorted( );

        verify( repository, never( ) ).save( any( ) );
        verifyNoInteractions( agendadorJob );
    }

    @Test
    @DisplayName("a prévia da simulação é a mesma que a criação devolve")
    void simular_concordaComAdicionar( )
    {
        LembreteInputDTO rascunho = todaSegundaAs9( );
        when( repository.save( any( LembreteEntity.class ) ) ).thenAnswer( inv -> inv.getArgument( 0 ) );

        List<LocalDateTime> previa = service.simular( rascunho );
        LembreteOutputDTO criado = service.adicionar( rascunho );

        assertThat( criado.proximasExecucoes( ) ).isEqualTo( previa );
    }

    @Test
    @DisplayName("simular aplica as recusas de domínio da criação: intervalo abaixo do piso")
    void simular_recusaIntervaloCurto( )
    {
        RecorrenciaInputDTO deMinutoEmMinuto = new RecorrenciaInputDTO( null, null, null, 1, null,
                List.of( ), List.of( ), null );
        NotificacaoInputDTO notificacao = new NotificacaoInputDTO( List.of( "pop-up" ), null,
                LocalDateTime.now( ), null, List.of( ) );

        assertThatThrownBy( ( ) -> service.simular(
                new LembreteInputDTO( "Água", null, 1L, deMinutoEmMinuto, notificacao ) ) )
                .isInstanceOf( StrategyException.class )
                .hasMessageContaining( "muito curto" );

        verify( repository, never( ) ).save( any( ) );
    }

    @Test
    @DisplayName("simular recusa o lembrete que nunca dispararia: datas todas no passado")
    void simular_recusaSerieEsgotada( )
    {
        NotificacaoInputDTO soPassado = new NotificacaoInputDTO( List.of( "pop-up" ), null, null, null,
                List.of( LocalDateTime.now( ).minusDays( 2 ) ) );

        assertThatThrownBy( ( ) -> service.simular(
                new LembreteInputDTO( "Já foi", null, 1L, null, soPassado ) ) )
                .isInstanceOf( StrategyException.class )
                .hasMessageContaining( "quando notificar" );
    }

    @Test
    @DisplayName("simular não olha a cota de lembretes: quem decide quando conferir a vaga é o chamador")
    void simular_naoConfereVaga( )
    {
        service.simular( todaSegundaAs9( ) );

        verify( repository, never( ) ).countBySessao_Uuid( any( ) );
    }
}
