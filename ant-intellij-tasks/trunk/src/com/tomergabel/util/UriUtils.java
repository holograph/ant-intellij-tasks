/*
	Copyright 2009 Tomer Gabel <tomer@tomergabel.com>

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


	ant-intellij-tasks project (http://code.google.com/p/ant-intellij-tasks/)

	$Id$
*/

package com.tomergabel.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A static container class for URI-related utility functions.
 */
public final class UriUtils {
    /**
     * Private c'tor.
     */
    private UriUtils() {
    }

    /**
     * A static URI which, when resolved against another URI, returns the other URI's parent.
     */
    private static final URI up = URI.create( ".." );

    /**
     * Returns the parent of the specified URI.
     * <p/>
     * For example, applying this method to the URI &quot;<tt>http://www.site.com/articles/article.html</tt>&quot; will
     * return the URI for &quot;<tt>http://www.site.com/articles/</tt>&quot;.
     *
     * @param uri The URI for which to return the parent.
     * @return The parent of the specified URI.
     * @throws IllegalArgumentException <ul> <li>The URI cannot be null></li> <li>Can't resolve parent for the specified
     *                                  URI.</li> </ul>
     */
    public static URI getParent( final URI uri ) throws IllegalArgumentException {
        if ( uri == null )
            throw new IllegalArgumentException( "The URI cannot be null." );

        final String path = uri.toString();
        final int finalSeparator = Math.max( path.lastIndexOf( '/' ), path.lastIndexOf( '\\' ) );
        final int extension = path.lastIndexOf( '.' );
        if ( extension > finalSeparator )
            try {
                // Extract all but final segment
                return new URI( path.substring( 0, finalSeparator + 1 ) ).normalize();
            } catch ( URISyntaxException e ) {
                throw new IllegalArgumentException( "Can't resolve parent for the specified URI.", e );
            }
        else
            return uri.resolve( up );
    }

    /**
     * Returns the filename for the specified URI.
     * <p/>
     * For example, applyign this method to the URI &quot;<tt>http://www.site.com/articles/article.html</tt>&quot; will
     * return &quot;<tt>article.html</tt>&quot;.
     *
     * @param uri The URI for which to return the filename.
     * @return The filename of the resource represented by the specified URI.
     * @throws IllegalArgumentException <ul><li>The URI cannot be null.</li><li>A separator character could not be found
     *                                  in the specified URI.</li><li>The specified URI does not point to a file
     *                                  resource.</li></ul>
     */
    public static String getFilename( final URI uri ) throws IllegalArgumentException {
        if ( uri == null )
            throw new IllegalArgumentException( "The URI cannot be null." );

        final String path = uri.getRawPath();
        final int finalSeparator = Math.max( path.lastIndexOf( '/' ), path.lastIndexOf( '\\' ) );
        if ( finalSeparator == -1 )
            throw new IllegalArgumentException( "A separator character could not found in the specified URI." );
        if ( finalSeparator == path.length() - 1 )
            throw new IllegalArgumentException( "The specified URI does not point to a file resource." );
        return path.substring( finalSeparator + 1 );
    }

    /**
     * Resolves the specified URI, and returns an absolute file system path to the resource represented by the URI.
     *
     * @param uri The URI for which to return an absolute path.
     * @return A file system path to the resource represented by the specified URI.
     * @throws IllegalArgumentException <ul><li>The URI cannot be null.</li><li>Wrong URI scheme for path resolution;
     *                                  only file:// URIs are supported.</li></ul>
     */
    public static String getPath( final URI uri ) throws IllegalArgumentException {
        return getFile( uri ).getAbsolutePath();
    }

    /**
     * Resolves the specified URI, and returns the file represented by the URI.
     *
     * @param uri The URI for which to return an absolute path.
     * @return The {@link File} instance represented by the specified URI.
     * @throws IllegalArgumentException <ul><li>The URI cannot be null.</li><li>Wrong URI scheme for path resolution;
     *                                  only file:// URIs are supported.</li></ul>
     */
    public static File getFile( URI uri ) throws IllegalArgumentException {
        if ( uri == null )
            throw new IllegalArgumentException( "The URI cannot be null." );
        if ( !"file".equals( uri.getScheme() ) )
            throw new IllegalArgumentException(
                    "Wrong URI scheme for path resolution, expected \"file\" " + "and got \"" + uri.getScheme() +
                            "\"" );

        // Workaround for the following bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5086147
        // Remove (or add, take your pick...) extra slashes after the scheme part.
        if ( uri.getAuthority() != null )
            try {
                uri = new URI( uri.toString().replace( "file://", "file:/" ) );
            } catch ( URISyntaxException e ) {
                throw new IllegalArgumentException( "The specified URI contains an authority, but could not be " +
                        "normalized.", e );
            }

        return new File( uri );
    }
}
