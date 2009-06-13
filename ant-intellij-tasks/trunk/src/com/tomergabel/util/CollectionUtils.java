package com.tomergabel.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    private static class MappingIterator<T, U> implements Iterator<U> {
        private final Iterator<T> iterator;
        private final Mapper<T, U> mapper;

        public MappingIterator( Iterable<T> source, Mapper<T, U> mapper ) {
            this( source.iterator(), mapper );
        }

        public MappingIterator( Iterator<T> iterator, Mapper<T, U> mapper ) {
            this.iterator = iterator;
            this.mapper = mapper;
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public U next() {
            return this.mapper.map( this.iterator.next() );
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static <T, U> Collection<U> map( final Collection<T> source, final Mapper<T, U> mapper ) {
        return new Collection<U>() {
            @Override
            public int size() {
                return source.size();
            }

            @Override
            public boolean isEmpty() {
                return source.isEmpty();
            }

            @Override
            public boolean contains( final Object o ) {
                for ( U item : this )
                    if ( item == null ? o == null : item.equals( o ) )
                        return true;
                return false;
            }

            @Override
            public Iterator<U> iterator() {
                return new MappingIterator<T, U>( source.iterator(), mapper );
            }

            @Override
            public Object[] toArray() {
                return new ArrayList<U>( this ).toArray();
            }

            @SuppressWarnings( { "unchecked" } )
            @Override
            public <V> V[] toArray( final V[] a ) {
                if ( a.length < source.size() )
                    return (V[]) toArray();
                int index = 0;
                try {
                    for ( T item : source )
                        a[ index++ ] = (V) mapper.map( item );
                } catch ( ClassCastException e ) {
                    throw new ArrayStoreException( e.getMessage() );
                }
                if ( index < source.size() )
                    a[ index ] = null;
                return a;
            }

            @Override
            public boolean add( final U u ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove( final Object o ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean containsAll( final Collection<?> c ) {
                for ( Object o : c )
                    if ( !contains( o ) )
                        return false;
                return true;
            }

            @Override
            public boolean addAll( final Collection<? extends U> c ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removeAll( final Collection<?> c ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean retainAll( final Collection<?> c ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static <T, U> Iterable<U> map( final Iterable<T> source, final Mapper<T, U> mapper ) {
        return new Iterable<U>() {
            @Override
            public Iterator<U> iterator() {
                return new MappingIterator( source, mapper );
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
