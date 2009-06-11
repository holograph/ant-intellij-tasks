package com.tomergabel.build.intellij;

import com.tomergabel.util.Lazy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.Callable;

public final class Resolver {
    private HashMap<String, String> properties;
    private HashMap<URI, Lazy<Module>> moduleDescriptorMap;
    private HashMap<String, Lazy<Module>> moduleNameMap;

    public Resolver( final Project project, final Module module ) throws ResolutionException {
        // Resolve properties: module overrides project
        this.properties = new HashMap<String, String>();
        if ( project != null )
            this.properties.putAll( project.getProperties() );
        if ( module != null )
            this.properties.putAll( module.getProperties() );

        // Load project
        if ( project != null ) {
            this.moduleNameMap = new HashMap<String, Lazy<Module>>();
            this.moduleDescriptorMap = new HashMap<URI, Lazy<Module>>();
            for ( String moduleUrl : project.getModules() ) {
                // Resolve URL and add to list
                final URI resolvedDescriptor = resolveUri( moduleUrl );
                this.moduleDescriptorMap.put( resolvedDescriptor, new Lazy<Module>( new Callable<Module>() {
                    @Override
                    public Module call() throws Exception {
                        return Module.parse( resolvedDescriptor );
                    }
                } ) );
            }

            // Override current module with preloaded instance
            final Lazy<Module> knownModule = new Lazy<Module>( module );
            this.moduleDescriptorMap.put( module.getDescriptor(), knownModule );
            this.moduleNameMap.put( module.getName(), knownModule );
        }
    }

    public URI resolveUri( String string ) throws IllegalArgumentException, ResolutionException {
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
            final String propertyValue = this.properties.get( propertyName );
            if ( propertyValue == null ) {
                final ResolutionException e = new ResolutionException();
                e.setProperty( propertyName );
                throw e;
            }

            // Append property and skip trailing backslashes
            sb.append( propertyValue );
            if ( propertyValue.endsWith( "/" ) && segmentIndex < string.length() &&
                    string.charAt( segmentIndex ) == '/' )
                ++segmentIndex;
        }

        // Append final segment and return resolved string
        sb.append( string.substring( segmentIndex ) );
        try {
            return new URI( sb.toString() );
        } catch ( URISyntaxException e ) {
            throw new ResolutionException( "Invalid expanded URI generated: " + sb.toString(), e );
        }
    }

    public static URI resolveUri( Project project, Module module, String string )
            throws IllegalArgumentException, ResolutionException {
        return new Resolver( project, module ).resolveUri( string );
    }
}
