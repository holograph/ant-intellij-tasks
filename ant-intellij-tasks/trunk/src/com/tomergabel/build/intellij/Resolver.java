package com.tomergabel.build.intellij;

import java.util.HashMap;

public final class Resolver {
    private HashMap<String, Object> properties;

    public Resolver( final Project project, final Module module ) {
        // Resolve properties: module overrides project
        this.properties = new HashMap<String, Object>();
        if ( project != null )
            this.properties.putAll( project.getProperties() );
        if ( module != null )
            this.properties.putAll( module.getProperties() );
    }

    public String resolve( String string ) throws IllegalArgumentException, PropertyResolutionException {
        if ( string == null )
            return null;

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
                throw new IllegalArgumentException( "Unmatched escape character $ in string \"" + string + "\"" );

            // Extract property name and advance segment pointer
            final String propertyName = string.substring( segmentIndex, next );
            segmentIndex = next + 1;

            // Expand property
            final Object property = this.properties.get( propertyName );
            if ( property == null )
                throw new PropertyResolutionException( propertyName );
            sb.append( property );
        }

        // Append final segment and return resolved string
        sb.append( string.substring( segmentIndex ) );
        return sb.toString();
    }

    public static String resolve( Project project, Module module, String string )
            throws IllegalArgumentException, PropertyResolutionException {
        return new Resolver( project, module ).resolve( string );
    }
}
