package com.routine.pusher.core.domain.lembrete;

import com.routine.pusher.core.domain.recorrencia.Recorrencia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guarda a regressão do 'quantidade': um lembrete recorrente ILIMITADO (sem quantidade) precisa
 * preservar {@code quantidade == null} no round-trip domínio -> entidade -> domínio. Se a entidade
 * usasse {@code int} primitivo, o default 0 seria lido como "cota esgotada" e o job seria removido
 * após o 1º disparo (ver {@link Recorrencia#temQuantidadeRestante()}).
 */
class LembreteMapperRecorrenciaTest
{
    private final LembreteMapper mapper = new LembreteMapperImpl( );

    @Test
    @DisplayName("recorrência ilimitada mantém quantidade nula ao ir e voltar de entidade")
    void recorrenciaIlimitada_preservaQuantidadeNula( )
    {
        Recorrencia recorrencia = new Recorrencia( );
        recorrencia.setQuantidade( null );          // ilimitado
        recorrencia.setIntervaloHoras( 2 );

        Lembrete lembrete = new Lembrete( );
        lembrete.setTitulo( "Beber água" );
        lembrete.setRecorrencia( recorrencia );

        Lembrete roundTrip = mapper.toDomain( mapper.toEntity( lembrete ) );

        assertThat( roundTrip.getRecorrencia( ).getQuantidade( ) ).isNull( );
        assertThat( roundTrip.getRecorrencia( ).temQuantidadeRestante( ) ).isTrue( );
    }

    @Test
    @DisplayName("recorrência por quantidade preserva o valor ao ir e voltar de entidade")
    void recorrenciaPorQuantidade_preservaValor( )
    {
        Recorrencia recorrencia = new Recorrencia( );
        recorrencia.setQuantidade( 3 );
        recorrencia.setIntervaloHoras( 1 );

        Lembrete lembrete = new Lembrete( );
        lembrete.setTitulo( "Tomar remédio" );
        lembrete.setRecorrencia( recorrencia );

        Lembrete roundTrip = mapper.toDomain( mapper.toEntity( lembrete ) );

        assertThat( roundTrip.getRecorrencia( ).getQuantidade( ) ).isEqualTo( 3 );
    }
}
