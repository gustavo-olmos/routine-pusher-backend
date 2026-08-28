package com.routine.pusher.core.enums;

import com.routine.pusher.core.domain.feriado.port.FeriadoPort;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * O que fazer quando uma ocorrência cai em dia não útil — fim de semana ou feriado.
 *
 * <p>Não é um booleano porque as respostas legítimas divergem: uma daily de equipe deve pular o
 * feriado, um remédio deve continuar disparando, e um boleto precisa ser antecipado, nunca
 * adiado.</p>
 */
public enum EnumPoliticaDiaUtil
{
    /** Padrão: o calendário não interfere. */
    IGNORAR
    {
        @Override
        public List<LocalDateTime> aplicar( List<LocalDateTime> ocorrencias, FeriadoPort feriados )
        {
            return ocorrencias;
        }
    },

    /** A ocorrência em dia não útil simplesmente não acontece. */
    PULAR
    {
        @Override
        public List<LocalDateTime> aplicar( List<LocalDateTime> ocorrencias, FeriadoPort feriados )
        {
            return ocorrencias.stream( )
                    .filter( ocorrencia -> ehDiaUtil( ocorrencia.toLocalDate( ), feriados ) )
                    .toList( );
        }
    },

    /** A ocorrência anda para o próximo dia útil, no mesmo horário. */
    ADIAR
    {
        @Override
        public List<LocalDateTime> aplicar( List<LocalDateTime> ocorrencias, FeriadoPort feriados )
        {
            return mover( ocorrencias, feriados, 1 );
        }
    },

    /** A ocorrência volta para o dia útil anterior — o caso do boleto, que não pode atrasar. */
    ANTECIPAR
    {
        @Override
        public List<LocalDateTime> aplicar( List<LocalDateTime> ocorrencias, FeriadoPort feriados )
        {
            return mover( ocorrencias, feriados, -1 );
        }
    };

    /**
     * O Carnaval encosta até quatro dias não úteis seguidos. O teto está aqui para que uma fonte de
     * feriados defeituosa — devolvendo o ano inteiro como feriado — não vire laço infinito dentro
     * de um cálculo que roda a cada serialização.
     */
    private static final int MAXIMO_DIAS_DESLOCADOS = 10;

    public abstract List<LocalDateTime> aplicar( List<LocalDateTime> ocorrencias, FeriadoPort feriados );

    public static boolean ehDiaUtil( LocalDate data, FeriadoPort feriados )
    {
        if( data.getDayOfWeek( ) == DayOfWeek.SATURDAY || data.getDayOfWeek( ) == DayOfWeek.SUNDAY )
            return false;

        return !feriados.ehFeriado( data );
    }

    /**
     * Duas ocorrências vizinhas podem parar no mesmo dia útil — sábado e domingo adiados viram a
     * mesma segunda. Daí o distinct: a série resultante não pode repetir horário.
     */
    private static List<LocalDateTime> mover( List<LocalDateTime> ocorrencias, FeriadoPort feriados, int passo )
    {
        LocalDateTime agora = LocalDateTime.now( );

        return ocorrencias.stream( )
                .map( ocorrencia -> deslocar( ocorrencia, feriados, passo ) )
                .filter( ocorrencia -> ocorrencia != null && ocorrencia.isAfter( agora ) )
                .distinct( )
                .sorted( )
                .toList( );
    }

    private static LocalDateTime deslocar( LocalDateTime ocorrencia, FeriadoPort feriados, int passo )
    {
        LocalDateTime deslocada = ocorrencia;

        for ( int tentativa = 0; tentativa < MAXIMO_DIAS_DESLOCADOS; tentativa++ ) {
            if( ehDiaUtil( deslocada.toLocalDate( ), feriados ) )
                return deslocada;

            deslocada = deslocada.plusDays( passo );
        }

        return null;
    }
}
