package com.tomergabel.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public final class UriUtils {
    private UriUtils() {
    }

    private static URI up = URI.create( ".." );

    public static URI getParent( URI uri ) throws IllegalArgumentException {
        final String path = uri.toString();
        int finalSeparator = Math.max( path.lastIndexOf( '/' ), path.lastIndexOf( '\\' ) );
        int extension = path.lastIndexOf( '.' );
        if ( extension > finalSeparator )
            try {
                // Extract all but final segment
                return new URI( path.substring( 0, finalSeparator + 1 ) ).normalize();
            } catch ( URISyntaxException e ) {
                throw new IllegalArgumentException( "Can't resolve parent for specified URI.", e );
            }
        else
            return uri.resolve( up );
    }

    public static String getFilename( final URI uri ) throws IllegalArgumentException {
        if ( uri == null )
            throw new IllegalArgumentException( "URI cannot be null." );

        final String path = uri.getRawPath();
        int finalSeparator = Math.max( path.lastIndexOf( '/' ), path.lastIndexOf( '\\' ) );
        if ( finalSeparator == -1 )
            throw new IllegalArgumentException( "Separator character not found in specified URI." );
        if ( finalSeparator == path.length() - 1 )
            throw new IllegalArgumentException( "Specified URI does not point to a file resource." );
        return path.substring( finalSeparator + 1 );
    }

    public static String getPath( URI uri ) throws IllegalArgumentException {
        if ( uri == null )
            throw new IllegalArgumentException( "URI cannot be null." );
        if ( !uri.getScheme().equals( "file" ) )
            throw new IllegalArgumentException(
                    "Wrong URI scheme for path resolution, expected \"file\" " + "and got \"" + uri.getScheme() +
                            "\"" );
        return new File( uri ).getAbsolutePath();
    }
}
