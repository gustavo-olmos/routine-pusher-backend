package com.routine.pusher.infrastructure.common.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Comparator;

public class SortInfo<T> implements Comparator<T>
{
    private final Logger LOGGER = LoggerFactory.getLogger( SortInfo.class );

    private final String atributoOrdenacao;
    private final boolean ordemReversa;

    public SortInfo( String atributoOrdenacao, boolean ordemReversa )
    {
        this.atributoOrdenacao = atributoOrdenacao;
        this.ordemReversa = ordemReversa;
    }

    @Override
    public int compare( T obj1, T obj2 )
    {
        try {
            Field field = obj1.getClass( ).getDeclaredField( atributoOrdenacao );
            field.setAccessible( true );

            Object rawValue1 = field.get( obj1 );
            Object rawValue2 = field.get( obj2 );

            int result = getComparacaoObjetos( rawValue1, rawValue2 );
            return ordemReversa ? -result : result;
        }
        catch ( NoSuchFieldException | IllegalAccessException e ) {
            LOGGER.debug("Atributo de ordenação {} não encontrado ou sem acesso.", atributoOrdenacao);

            throw new RuntimeException("Erro ao comparar objetos: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private int getComparacaoObjetos( Object rawValue1, Object rawValue2 ) throws IllegalAccessException
    {
        if( rawValue1 == null || rawValue2 == null ) {
            LOGGER.warn("Valor passado possivelmente nulo");
            return ( rawValue1 == null ) ? (rawValue2 == null ? 0 : -1) : 1;
        }

        if( !( rawValue1 instanceof Comparable ) || !( rawValue2 instanceof Comparable) ) {
            throw new RuntimeException( "O atributo de ordenação não é implementável");
        }

        Comparable<Object> value1 = (Comparable<Object>) rawValue1;
        Comparable<Object> value2 = (Comparable<Object>) rawValue2;

        return value1.compareTo( value2 );
    }
}
