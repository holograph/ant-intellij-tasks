package com.tomergabel.util;

import java.util.*;

public final class TestUtils {
    private TestUtils() {
    }

    private static <T> Collection<T> toCollection( final Iterator<T> iterator ) {
        final List<T> list = new ArrayList<T>();
        while ( iterator.hasNext() )
            list.add( iterator.next() );
        return list;
    }

    public static <T> void assertSetEquality( T[] expected, Iterator<T> actual ) {
        assertSetEquality( null, expected, toCollection( actual ) );
    }

    public static <T> void assertSetEquality( String message, T[] expected, Iterator<T> actual ) {
        assertSetEquality( null, expected, toCollection( actual ) );
    }

    public static <T> void assertSetEquality( Collection<T> expected, T[] actual ) {
        assertSetEquality( null, expected, actual );
    }

    public static <T> void assertSetEquality( T[] expected, Collection<T> actual ) {
        assertSetEquality( null, expected, actual );
    }

    public static <T> void assertSetEquality( T[] expected, T[] actual ) {
        assertSetEquality( null, expected, actual );
    }

    public static <T> void assertSetEquality( String message, Collection<T> expected, T[] actual ) {
        assertSetEquality( message, expected, Arrays.asList( actual ) );
    }

    public static <T> void assertSetEquality( String message, T[] expected, Collection<T> actual ) {
        assertSetEquality( message, Arrays.asList( expected ), actual );
    }

    public static <T> void assertSetEquality( String message, T[] expected, T[] actual ) {
        assertSetEquality( message, Arrays.asList( expected ), Arrays.asList( actual ) );
    }

    public static <T> void assertSetEquality( Collection<T> expected, Collection<T> actual ) {
        assertSetEquality( null, expected, actual );
    }

    public static <T> void assertSetEquality( String message, Collection<T> expected, Collection<T> actual ) {
        final HashSet<T> expectedSet = new HashSet<T>( expected );
        final HashSet<T> actualSet = new HashSet<T>( actual );
        expectedSet.removeAll( actual );
        actualSet.removeAll( expected );
        if ( expectedSet.size() == 0 && actualSet.size() == 0 )
            return;

        final StringBuilder sb = new StringBuilder();
        if ( message != null )
            sb.append( message ).append( Character.toChars( Character.LINE_SEPARATOR ) );
        sb.append( "Assertion failed: " );
        if ( expectedSet.size() > 0 ) {
            sb.append( "missing " ).append( expectedSet );
            if ( actualSet.size() > 0 )
                sb.append( ", " );
        }
        if ( actualSet.size() > 0 )
            sb.append( "unexpected " ).append( actualSet );
        throw new AssertionError( sb.toString() );
    }
}
