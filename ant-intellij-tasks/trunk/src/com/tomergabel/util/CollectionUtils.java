package com.tomergabel.util;

import java.util.*;

public final class CollectionUtils {
    private static final Object DEFAULT_JOIN_SEPARATOR = ',';
    private static final boolean DEFAULT_JOIN_NULL_BEHAVIOR = false;

    private CollectionUtils() {
    }

    private static class MappingIterator<T, U> implements Iterator<U> {
        private final Iterator<T> iterator;
        private final Mapper<T, U> mapper;

        public MappingIterator( final Iterable<T> source, final Mapper<T, U> mapper ) {
            this( source.iterator(), mapper );
        }

        public MappingIterator( final Iterator<T> iterator, final Mapper<T, U> mapper ) {
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
                for ( final U item : this )
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
                    for ( final T item : source )
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
                for ( final Object o : c )
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
                return new MappingIterator<T, U>( source, mapper );
            }
        };
    }

    public static <T> String join( final boolean renderNulls, final T... values ) {
        return join( Arrays.asList( values ), DEFAULT_JOIN_SEPARATOR, renderNulls );
    }

    public static <T> String join( final Object separator, final T... values ) {
        return join( Arrays.asList( values ), separator, DEFAULT_JOIN_NULL_BEHAVIOR );
    }

    public static <T> String join( final boolean renderNulls, final Object separator, final T... values ) {
        return join( Arrays.asList( values ), separator, renderNulls );
    }

    public static <T> String join( final Iterable<T> source ) {
        return join( source, DEFAULT_JOIN_SEPARATOR, DEFAULT_JOIN_NULL_BEHAVIOR );
    }

    public static <T> String join( final Iterable<T> source, final boolean renderNulls ) {
        return join( source, DEFAULT_JOIN_SEPARATOR, renderNulls );
    }

    public static <T> String join( final Iterable<T> source, final Object separator ) {
        return join( source, separator, DEFAULT_JOIN_NULL_BEHAVIOR );
    }
    
    public static <T> String join( final Iterable<T> source, final Object separator, final boolean renderNulls ) {
        final StringBuilder sb = new StringBuilder();
        boolean first = true;

        for ( final T value : source ) {
            if ( value == null && !renderNulls )
                continue;
            
            if ( !first )
                sb.append( separator );
            else
                first = false;

            sb.append( value );
        }
        return sb.toString();
    }

    public static <T> List<T> toList( final Iterator<T> iterator ) {
        final List<T> list = new ArrayList<T>();
        while ( iterator.hasNext() )
            list.add( iterator.next() );
        return list;
    }

    public static <T> boolean setEquals( final T[] expected, final Iterator<T> actual ) {
        return setEquals( Arrays.asList( expected ), toList( actual ) );
    }

    public static <T> boolean setEquals( final T[] expected, final Collection<T> actual ) {
        return setEquals( Arrays.asList( expected ), actual );
    }

    public static <T> boolean setEquals( final T[] expected, final T[] actual ) {
        return setEquals( Arrays.asList( expected ), Arrays.asList( actual ) );
    }

    public static <T> boolean setEquals( final Collection<T> expected, final Collection<T> actual ) {
        if ( expected == null )
            return actual == null;
        if ( actual == null )
            return false;

        final HashSet<T> expectedSet = new HashSet<T>( expected );
        final HashSet<T> actualSet = new HashSet<T>( actual );
        expectedSet.removeAll( actual );
        actualSet.removeAll( expected );
        return expectedSet.size() == 0 && actualSet.size() == 0;
    }

    public static <T> int deepHashCode( final Iterable<T> stream ) {
        int hash = 0;
        for ( final T next : stream )
            hash = 31 * hash + ( next == null ? 0 : next.hashCode() );
        return hash;
    }

    private static class PredicateIterator<T> implements Iterator<T> {
        private final Iterator<T> source;
        private final Predicate<T> predicate;
        private T next;

        private PredicateIterator( final Iterator<T> source, final Predicate<T> predicate ) {
            if ( source == null )
                throw new IllegalArgumentException( "The source iterator cannot be null." );
            if ( predicate == null )
                throw new IllegalArgumentException( "The predicate cannot be null." );

            this.source = source;
            this.predicate = predicate;
            findNext();
        }

        private void findNext() {
            while ( source.hasNext() ) {
                final T x = source.next();
                if ( this.predicate.evaluate( x ) ) {
                    this.next = x;
                    return;
                }
            }
            this.next = null;
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public T next() {
            final T x = this.next;
            findNext();
            return x;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static <T> Iterable<T> filter( final Iterable<T> source, final Predicate<T> predicate ) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new PredicateIterator<T>( source.iterator(), predicate );
            }
        };
    }

    @SuppressWarnings( { "unchecked" } )
    public static <T> Iterable<T> filter( final Iterable<?> source, final Class<T> type ) {
        return (Iterable<T>) filter( (Iterable<Object>) source, new Predicate<Object>() {
            @Override
            public boolean evaluate( final Object x ) {
                return type.isInstance( x );
            }
        } );
    }

    private static class ConcatenationIterator<T> implements Iterator<T> {
        final Queue<Iterator<T>> sources;
        T next;

        private ConcatenationIterator( final Iterable<T>[] sources ) {
            this.sources = new ArrayDeque<Iterator<T>>( sources.length );
            for ( final Iterable<T> source : sources )
                this.sources.add( source.iterator() );
        }

        @Override
        public boolean hasNext() {
            while ( sources.peek() != null ) {
                if ( sources.peek().hasNext() )
                    return true;
                sources.poll();
            }
            return false;
        }

        @Override
        public T next() {
            while ( sources.peek() != null ) {
                if ( sources.peek().hasNext() )
                    return sources.peek().next();
                sources.poll();
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static <T> Iterable<T> concat( final Iterable<T>... sources ) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new ConcatenationIterator<T>( sources );
            }
        };
    }
}
