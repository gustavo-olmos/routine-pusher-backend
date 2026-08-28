package com.routine.pusher.core.domain.feriado.adapter;

import com.routine.pusher.core.domain.feriado.port.FeriadoPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feriados nacionais brasileiros calculados, não consultados.
 *
 * <p>É uma escolha deliberada: o calendário nacional é determinístico — datas fixas mais as móveis
 * derivadas da Páscoa — então dá para computá-lo. Isso elimina de uma vez a rede no caminho quente,
 * a indisponibilidade de terceiro, o cache negativo e o dado velho. Trocar por uma fonte externa é
 * implementar {@link FeriadoPort} de outro jeito; nada no domínio muda.</p>
 *
 * <p>O que este adaptador <b>não</b> cobre: feriado estadual e municipal. Esses não são calculáveis,
 * e é justamente onde uma fonte externa (ou cadastro do próprio usuário) passa a ser necessária.</p>
 */
@Component
public class FeriadoNacionalBrasilAdapter implements FeriadoPort
{
    /**
     * O ano vem de data escolhida pelo usuário: sem teto, uma lista de datas espalhadas por séculos
     * encheria o mapa indefinidamente. Passado o teto o cálculo continua, só deixa de ser guardado.
     */
    private static final int MAXIMO_ANOS_EM_CACHE = 8;

    private final Map<Integer, Set<LocalDate>> porAno = new ConcurrentHashMap<>( );

    @Override
    public boolean ehFeriado( LocalDate data )
    {
        return feriadosDe( data.getYear( ) ).contains( data );
    }

    public Set<LocalDate> feriadosDe( int ano )
    {
        Set<LocalDate> emCache = porAno.get( ano );
        if( emCache != null ) return emCache;

        Set<LocalDate> calculados = calcular( ano );

        if( porAno.size( ) < MAXIMO_ANOS_EM_CACHE )
            porAno.putIfAbsent( ano, calculados );

        return calculados;
    }

    private Set<LocalDate> calcular( int ano )
    {
        LocalDate pascoa = pascoa( ano );

        // HashSet e não Set.of: a Sexta-feira Santa pode cair em 21 de abril (Páscoa em 23) e
        // colidir com Tiradentes. Set.of recusa duplicata e derrubaria o cálculo naquele ano.
        Set<LocalDate> feriados = new HashSet<>( );

        feriados.add( LocalDate.of( ano, 1, 1 ) );      // Confraternização Universal
        feriados.add( pascoa.minusDays( 48 ) );         // Carnaval (segunda)
        feriados.add( pascoa.minusDays( 47 ) );         // Carnaval (terça)
        feriados.add( pascoa.minusDays( 2 ) );          // Sexta-feira Santa
        feriados.add( LocalDate.of( ano, 4, 21 ) );     // Tiradentes
        feriados.add( LocalDate.of( ano, 5, 1 ) );      // Dia do Trabalho
        feriados.add( pascoa.plusDays( 60 ) );          // Corpus Christi
        feriados.add( LocalDate.of( ano, 9, 7 ) );      // Independência
        feriados.add( LocalDate.of( ano, 10, 12 ) );    // Nossa Senhora Aparecida
        feriados.add( LocalDate.of( ano, 11, 2 ) );     // Finados
        feriados.add( LocalDate.of( ano, 11, 15 ) );    // Proclamação da República
        feriados.add( LocalDate.of( ano, 11, 20 ) );    // Consciência Negra
        feriados.add( LocalDate.of( ano, 12, 25 ) );    // Natal

        return Set.copyOf( feriados );
    }

    /**
     * Algoritmo de Meeus/Jones/Butcher para o calendário gregoriano. Carnaval, Sexta-feira Santa e
     * Corpus Christi são todos deslocamentos fixos a partir daqui.
     */
    private LocalDate pascoa( int ano )
    {
        int a = ano % 19;
        int b = ano / 100;
        int c = ano % 100;
        int d = b / 4;
        int e = b % 4;
        int f = ( b + 8 ) / 25;
        int g = ( b - f + 1 ) / 3;
        int h = ( 19 * a + b - d - g + 15 ) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = ( 32 + 2 * e + 2 * i - h - k ) % 7;
        int m = ( a + 11 * h + 22 * l ) / 451;

        int mes = ( h + l - 7 * m + 114 ) / 31;
        int dia = ( ( h + l - 7 * m + 114 ) % 31 ) + 1;

        return LocalDate.of( ano, mes, dia );
    }
}
