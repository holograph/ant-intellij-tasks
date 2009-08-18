package com.tomergabel.build.intellij.model;

import com.tomergabel.util.Lazy;
import com.tomergabel.util.LazyInitializationException;
import com.tomergabel.util.UriUtils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

public abstract class PropertyResolver {
    private final Lazy<Map<String, String>> propertyCache = new Lazy<Map<String, String>>() {
        @Override
        public Map<String, String> call() {
            return Collections.unmodifiableMap( PropertyResolver.this.generatePropertyMap() );
        }

    };

    protected abstract Map<String, String> generatePropertyMap();

    private final PropertyResolver parent;

    public PropertyResolver( final PropertyResolver parent ) {
        this.parent = parent;
    }

    public URI resolveUriString( final String string ) throws IllegalArgumentException, ResolutionException {
        if ( string == null )
            return null;

        // Expand embedded properties
        final String expandedString = expandProperties( string );
        final URI expandedUri;
        try {
            expandedUri = new URI( expandedString ).normalize();
        } catch ( URISyntaxException e ) {
            throw new ResolutionException( "Invalid expanded URI generated: " + expandedString, e );
        }

        // Normalize URI
        if ( expandedUri.getScheme() == null )
            throw new ResolutionException( "No scheme detected for URI '" + expandedUri + "'." );

        if ( "file".equals( expandedUri.getScheme() ) )
            return expandedUri;
        else if ( "jar".equals( expandedUri.getScheme() ) ) {
            // Construct new URI: replace schema with "file" and strip trailing !/
            final String newUri = expandedUri.toString().replace( "jar:", "file:" );
            try {
                return new URI( newUri.endsWith( "!/" ) ? newUri.substring( 0, newUri.length() - 2 ) : newUri );
            } catch ( URISyntaxException e ) {
                throw new ResolutionException( "JAR URI resolution failed", e );
            }
        } else throw new ResolutionException( "Unrecognized URI scheme \"" + expandedUri.getScheme() + "\"" );
    }

    public File resolveUriFile( final String string ) throws IllegalArgumentException, ResolutionException {
        return UriUtils.getFile( resolveUriString( string ) );
    }
    
    public String getPropertyValue( final String property ) throws IllegalArgumentException {
        if ( property == null )
            throw new IllegalArgumentException( "The property name cannot be null." );

        final String value;
        try {
            value = this.propertyCache.get().get( property );
        } catch ( LazyInitializationException e ) {
            // Safety net, should never happen
            throw new RuntimeException( e.getCause() );
        }

        if ( value != null )
            return value;
        else if ( this.parent != null )
            return parent.getPropertyValue( property );
        else
            return null;
    }

    public String expandProperties( final String string ) throws IllegalArgumentException, ResolutionException {
        if ( string == null )
            throw new IllegalArgumentException( "The string cannot be null." );

        // Look up and expand all macros
        int segmentIndex = 0;
        int next;
        final StringBuilder sb = new StringBuilder();
        while ( ( next = string.indexOf( '$', segmentIndex ) ) > -1 ) {
            // Append up to match point
            sb.append( string.substring( segmentIndex, next ) );

            // Look up next dollar sign
            segmentIndex = next + 1;
            next = string.indexOf( '$', segmentIndex );
            if ( next == -1 )
                throw new ResolutionException( "Unmatched escape character $ in string \"" + string + "\"" );

            // Extract property name and advance segment pointer
            final String propertyName = string.substring( segmentIndex, next );
            segmentIndex = next + 1;

            // Inspect property for special behavior
            if ( "APPLICATION_HOME_DIR".equals( propertyName ) )
                throw new ResolutionException( propertyName, "Component depends on a JAR located under the " +
                        "IntelliJ IDEA home directory. This is not supported. Please replace the dependency " +
                        "with a project- or module-level library dependency." );

            // Expand property
            final String propertyValue = getPropertyValue( propertyName );
            if ( propertyValue == null )
                throw new ResolutionException( propertyName, "Could not resolve property '" + propertyName + "'" );

            // Append property and skip trailing backslashes
            sb.append( propertyValue );
            if ( propertyValue.endsWith( "/" ) && segmentIndex < string.length() &&
                    string.charAt( segmentIndex ) == '/' )
                ++segmentIndex;
        }

        // Append final segment and return resolved string
        sb.append( string.substring( segmentIndex ) );
        return sb.toString();
    }
}
