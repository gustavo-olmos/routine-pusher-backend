package com.routine.pusher.application.job;

import com.routine.pusher.application.service.LembreteSSEService;
import com.routine.pusher.application.service.LembreteService;
import com.routine.pusher.core.domain.categoria.CategoriaEntity;
import com.routine.pusher.core.domain.categoria.CategoriaRepository;
import com.routine.pusher.core.domain.lembrete.dto.LembreteInputDTO;
import com.routine.pusher.core.domain.notificacao.dto.NotificacaoInputDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração do "motor" de notificação: valida que um lembrete agendado realmente
 * dispara o job Quartz (com dependências injetadas pelo Spring) e emite no fluxo SSE — o que
 * antes falhava porque o Scheduler estático deixava o job sem dependências. Também exercita a
 * persistência das coleções (@ElementCollection) e a back-reference do MapStruct.
 */
@SpringBootTest
class LembreteNotificacaoEngineTest
{
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private LembreteService lembreteService;
    @Autowired
    private LembreteSSEService notificacaoService;

    @Test
    void lembreteAgendadoDisparaJobEEmiteNoFluxoSSE( ) throws InterruptedException
    {
        CategoriaEntity categoria = new CategoriaEntity( );
        categoria.setNome( "Casa" );
        categoria.setCor( "azul" );
        categoria.setFatorOrdem( 1 );
        Long categoriaId = categoriaRepository.save( categoria ).getId( );

        CountDownLatch latch = new CountDownLatch( 1 );
        AtomicReference<String> recebido = new AtomicReference<>( );

        Flux<String> fluxo = notificacaoService.obterFluxoNotificacoes( );
        Disposable inscricao = fluxo.subscribe( mensagem -> {
            recebido.set( mensagem );
            latch.countDown( );
        } );

        LocalDateTime daquiA2s = LocalDateTime.now( ).plusSeconds( 2 );
        NotificacaoInputDTO notificacao = new NotificacaoInputDTO(
                List.of( "som" ), null, null, null, List.of( daquiA2s ) );
        LembreteInputDTO input = new LembreteInputDTO(
                "Regar plantas", "no jardim", categoriaId, null, notificacao );

        lembreteService.adicionar( input );

        boolean disparou = latch.await( 10, TimeUnit.SECONDS );
        inscricao.dispose( );

        assertThat( disparou )
                .as( "o job Quartz deveria ter disparado e emitido no fluxo SSE em até 10s" )
                .isTrue( );
        assertThat( recebido.get( ) ).isEqualTo( "Regar plantas" );
    }
}
