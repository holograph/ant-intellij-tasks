package com.tomergabel.util;

import java.util.Iterator;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T, U> Iterable<U> map( final Iterable<T> source, final Mapper<T, U> mapper ) {
        return new Iterable<U>() {
            @Override
            public Iterator<U> iterator() {
                return new Iterator<U>() {
                    private Iterator<T> sourceIter = source.iterator();

                    @Override
                    public boolean hasNext() {
                        return sourceIter.hasNext();
                    }

                    @Override
                    public U next() {
                        return mapper.map( sourceIter.next() );
                    }

                    @Override
                    public void remove() {
                        sourceIter.remove();
                    }
                };
            }
        };
    }

    public static <T> String join( final Iterable<T> source, final Object separator ) {
        final StringBuilder sb = new StringBuilder();
        boolean first = true;

        for ( T value : source ) {
            if ( !first )
                sb.append( separator );
            else
                first = false;

            sb.append( value );
        }
        return sb.toString();
    }
}
