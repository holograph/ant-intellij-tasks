package com.tomergabel.util;

import java.util.*;

public final class TestUtils {
    private TestUtils() {
    }

    public static <T> void assertSetEquality( final T[] expected, final Iterator<T> actual ) {
        assertSetEquality( null, expected, CollectionUtils.toList( actual ) );
    }

    public static <T> void assertSetEquality( final String message, final T[] expected, final Iterator<T> actual ) {
        assertSetEquality( message, expected, CollectionUtils.toList( actual ) );
    }

    public static <T> void assertSetEquality( final Collection<T> expected, final T[] actual ) {
        assertSetEquality( null, expected, actual );
    }

    public static <T> void assertSetEquality( final T[] expected, final Collection<T> actual ) {
        assertSetEquality( null, expected, actual );
    }

    public static <T> void assertSetEquality( final T[] expected, final T[] actual ) {
        assertSetEquality( null, expected, actual );
    }

    public static <T> void assertSetEquality( final String message, final Collection<T> expected, final T[] actual ) {
        assertSetEquality( message, expected, Arrays.asList( actual ) );
    }

    public static <T> void assertSetEquality( final String message, final T[] expected, final Collection<T> actual ) {
        assertSetEquality( message, Arrays.asList( expected ), actual );
    }

    public static <T> void assertSetEquality( final String message, final T[] expected, final T[] actual ) {
        assertSetEquality( message, Arrays.asList( expected ), Arrays.asList( actual ) );
    }

    public static <T> void assertSetEquality( final Collection<T> expected, final Collection<T> actual ) {
        assertSetEquality( null, expected, actual );
    }

    public static <T> void assertSetEquality( final String message, final Collection<T> expected, final Collection<T> actual ) {
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
