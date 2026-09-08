package com.routine.pusher.infrastructure.common.shared;

import com.routine.pusher.infrastructure.exceptions.SortingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Ordena uma lista pelo nome de um atributo vindo da requisição ({@code sortInfo}).
 *
 * <p>O campo é resolvido <b>na construção</b>, e não na primeira comparação. A diferença não é
 * estética: {@code stream().sorted()} nunca chama o comparador numa lista de zero ou um elemento,
 * então validar tarde fazia {@code sortInfo} inválido responder 200 com uma lista e 400 com duas —
 * o mesmo pedido mudando de resultado conforme o acervo do visitante. Bug assim não reproduz para
 * quem testa com a base quase vazia.</p>
 */
public class SortInfo<T> implements Comparator<T>
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SortInfo.class );

    private final Field campo;
    private final boolean ordemReversa;

    public SortInfo( Class<T> tipo, String atributoOrdenacao, boolean ordemReversa )
    {
        this.campo = resolverCampo( tipo, atributoOrdenacao );
        this.ordemReversa = ordemReversa;
    }

    /**
     * Recusa cedo o atributo que não existe ou que não sabe se comparar, e diz quais servem — quem
     * chamou errado não tem como adivinhar a lista olhando a resposta de erro. Note que os campos
     * ordenáveis são os do <b>DTO de saída</b>: {@code id} funciona em categoria e não em lembrete,
     * cuja identidade pública é o {@code uuid}.
     */
    private Field resolverCampo( Class<T> tipo, String atributoOrdenacao )
    {
        try {
            Field field = tipo.getDeclaredField( atributoOrdenacao );

            if( !ehComparavel( field.getType( ) ) )
                throw new NoSuchFieldException( atributoOrdenacao );

            field.setAccessible( true );
            return field;
        }
        catch ( NoSuchFieldException | SecurityException e ) {
            LOGGER.debug("Atributo de ordenação {} não serve para {}", atributoOrdenacao, tipo.getSimpleName( ));

            throw new SortingException( "Atributo de ordenação inválido: " + atributoOrdenacao
                    + ". Use um destes: " + camposOrdenaveis( tipo ) );
        }
    }

    private String camposOrdenaveis( Class<T> tipo )
    {
        return Arrays.stream( tipo.getDeclaredFields( ) )
                .filter( f -> !f.isSynthetic( ) && !Modifier.isStatic( f.getModifiers( ) ) )
                .filter( f -> ehComparavel( f.getType( ) ) )
                .map( Field::getName )
                .collect( Collectors.joining( ", " ) );
    }

    /**
     * Primitivo conta como comparável: {@code int} não implementa {@link Comparable}, mas o valor
     * lido por reflexão chega aqui já boxeado em {@code Integer}, que implementa. Testar só o tipo
     * declarado rejeitaria {@code fatorOrdem} — campo que a listagem de categoria usa por padrão.
     */
    private static boolean ehComparavel( Class<?> tipo )
    {
        return tipo.isPrimitive( ) ? tipo != void.class : Comparable.class.isAssignableFrom( tipo );
    }

    @Override
    public int compare( T obj1, T obj2 )
    {
        try {
            int result = getComparacaoObjetos( campo.get( obj1 ), campo.get( obj2 ) );
            return ordemReversa ? -result : result;
        }
        catch ( IllegalAccessException e ) {
            throw new SortingException( "Erro ao comparar objetos: " + e.getMessage( ) );
        }
    }

    @SuppressWarnings("unchecked")
    private int getComparacaoObjetos( Object rawValue1, Object rawValue2 )
    {
        if( rawValue1 == null || rawValue2 == null ) {
            LOGGER.warn("Valor passado possivelmente nulo");
            return ( rawValue1 == null ) ? (rawValue2 == null ? 0 : -1) : 1;
        }

        Comparable<Object> value1 = (Comparable<Object>) rawValue1;
        Comparable<Object> value2 = (Comparable<Object>) rawValue2;

        return value1.compareTo( value2 );
    }
}
