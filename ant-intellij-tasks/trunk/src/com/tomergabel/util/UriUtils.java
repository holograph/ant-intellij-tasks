package com.tomergabel.util;

import java.net.URI;

public final class UriUtils {
    private UriUtils() {
    }

    private static URI up = URI.create( ".." );

    public static URI getParent( URI uri ) throws IllegalArgumentException {
        return uri.resolve( up );
    }

    public static String getFilename( final URI uri ) throws IllegalArgumentException {
        if ( uri == null )
            throw new IllegalArgumentException( "URI cannot be null.");

        final String path = uri.getRawPath();
        int finalSeparator = Math.max( path.lastIndexOf( '/' ), path.lastIndexOf( '\\' ) );
        if ( finalSeparator == -1 )
            throw new IllegalArgumentException( "Separator character not found in specified URI." );
        if ( finalSeparator == path.length() - 1 )
            throw new IllegalArgumentException( "Specified URI does not point to a file resource." );
        return path.substring( finalSeparator + 1 );
    }
}
