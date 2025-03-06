package com.routine.pusher.util;

import lombok.Setter;
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
    public int compare( Object obj1, Object obj2 )
    {
        try {
            Field field = obj1.getClass( ).getDeclaredField( atributoOrdenacao );
            field.setAccessible( true );

            Comparable<T> value1 = (Comparable<T>) field.get( obj1 );
            Comparable<T> value2 = (Comparable<T>) field.get( obj2 );

            int result = value1.compareTo( (T) value2 );

            return ordemReversa ? -result : result;
        }
        catch ( NoSuchFieldException | IllegalAccessException e ) {
            LOGGER.debug("Atributo não encontrado ou sem acesso");

            throw new RuntimeException("Erro ao comparar objetos: " + e.getMessage(), e);
        }
    }

    @Override
    public Comparator<T> reversed() {
        return new SortInfo<>( atributoOrdenacao, !ordemReversa );
    }
}
