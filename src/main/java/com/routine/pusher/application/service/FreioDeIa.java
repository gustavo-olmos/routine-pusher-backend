package com.routine.pusher.application.service;

import com.routine.pusher.infrastructure.exceptions.IaIndisponivelException;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Freio global das chamadas de IA, por cima da cota de cada sessão.
 *
 * <p>A cota por sessão protege o custo de um visitante; não protege o provedor de todos eles juntos.
 * Um post que dá certo traz gente em rajada, e quem limpa o cookie ganha sessão nova. Sem freio, o
 * free tier do Gemini estoura e cada visitante seguinte gasta uma tentativa para receber erro.</p>
 *
 * <ul>
 *   <li><b>Balde de fichas</b>: no máximo {@code porMinuto} chamadas por minuto, abaixo do teto do
 *   provedor, repostas aos poucos. Rajada além disso espera alguns segundos em vez de virar 429
 *   lá fora.</li>
 *   <li><b>Disjuntor</b>: depois que o provedor recusa por limite, a API para de chamá-lo por
 *   {@code pausa} e responde na hora, sem gastar cota de ninguém.</li>
 * </ul>
 *
 * <p>Em memória, porque roda uma instância só — como o Quartz, que também só vira cluster quando
 * houver duas.</p>
 */
@Component
public class FreioDeIa
{
    private static final Logger LOGGER = LoggerFactory.getLogger( FreioDeIa.class );

    private static final String OCUPADO =
            "Muita gente testando a IA agora; tente de novo em alguns segundos ou crie pelo formulário";

    private final Clock relogio;
    private final int porMinuto;
    private final Duration pausa;

    private double fichas;
    private Instant ultimaReposicao;
    private Instant pausadoAte = Instant.MIN;

    @Inject
    public FreioDeIa( @Value("${ia.freio.por-minuto:12}") int porMinuto,
                      @Value("${ia.freio.pausa-apos-recusa:60s}") Duration pausa )
    {
        this( Clock.systemUTC( ), porMinuto, pausa );
    }

    FreioDeIa( Clock relogio, int porMinuto, Duration pausa )
    {
        this.relogio = relogio;
        this.porMinuto = porMinuto;
        this.pausa = pausa;
        this.fichas = porMinuto;
        this.ultimaReposicao = relogio.instant( );
    }

    /**
     * Libera uma chamada ou recusa já, antes de qualquer gasto. Chamado depois da vaga de lembrete
     * e antes da cota da sessão: recusa aqui não consome nada do visitante.
     */
    public synchronized void autorizar( )
    {
        Instant agora = relogio.instant( );

        if( agora.isBefore( pausadoAte ) )
            throw new IaIndisponivelException( OCUPADO, Duration.between( agora, pausadoAte ), false );

        repor( agora );
        if( fichas < 1 ) {
            long msParaProxima = (long) Math.ceil( ( 1 - fichas ) * 60_000d / porMinuto );
            throw new IaIndisponivelException( OCUPADO, Duration.ofMillis( msParaProxima ), false );
        }
        fichas -= 1;
    }

    /** O provedor recusou: se foi por limite, ninguém chama de novo durante a pausa. */
    public synchronized void registrar( IaIndisponivelException falha )
    {
        if( !falha.recusaDoProvedor( ) ) return;

        Duration espera = falha.tenteEm( ) != null && falha.tenteEm( ).compareTo( pausa ) > 0
                ? falha.tenteEm( ) : pausa;
        pausadoAte = relogio.instant( ).plus( espera );
        LOGGER.warn( "Provedor de IA recusou por limite: chamadas pausadas por {}s", espera.toSeconds( ) );
    }

    private void repor( Instant agora )
    {
        double minutos = Duration.between( ultimaReposicao, agora ).toMillis( ) / 60_000d;
        fichas = Math.min( porMinuto, fichas + minutos * porMinuto );
        ultimaReposicao = agora;
    }
}
