package com.tomergabel.build.intellij;

import com.tomergabel.util.Lazy;
import com.tomergabel.util.LazyInitializationException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.Callable;

public final class Resolver {
    private final Project project;
    private final Module module;
    private final HashMap<URI, Lazy<Module>> moduleDescriptorMap;
    private final HashMap<String, Module> moduleNameMap;
    private final HashMap<String, String> properties;

    public Resolver( final Project project, final Module module ) throws ResolutionException {
        this.project = project;
        this.module = module;

        // Resolve properties: module overrides project
        this.properties = new HashMap<String, String>();

        // Load project
        if ( project != null ) {
            this.properties.putAll( project.getProperties() );
            this.moduleNameMap = new HashMap<String, Module>();
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
            this.moduleDescriptorMap.put( module.getDescriptor(), new Lazy<Module>( module ) );
            this.moduleNameMap.put( module.getName(), module );
        } else {
            this.moduleNameMap = null;
            this.moduleDescriptorMap = null;
        }

        // Override project properties with module properties, if applicable
        if ( module != null )
            this.properties.putAll( module.getProperties() );
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

    public Collection<Module> resolveModuleDependencies() throws IllegalStateException, ResolutionException {
        final Collection<Module> modules = new ArrayList<Module>( this.module.getDepdencies().size() );

        // Iterate dependencies and process module dependencies
dependency:
        for ( Dependency dependency : this.module.getDepdencies() )
            if ( dependency instanceof ModuleDependency ) {
                // Assert that a project was specified to resolve module dependencies
                if ( this.project == null )
                    throw new IllegalStateException( "Cannot resolve module dependencies, project not specified" );

                // Look up module in name map
                final String name = ( (ModuleDependency) dependency ).name;
                if ( this.moduleNameMap.containsKey( name ) ) {
                    modules.add( this.moduleNameMap.get( name ) );
                    continue;
                }

                // Iterate descriptor map. We'll have to lazy-load each module to extract its name
                for ( Lazy<Module> candidate : this.moduleDescriptorMap.values() ) {
                    try {
                        if ( candidate.get().getName().equals( name ) ) {
                            // Found! Update named map, add to module list
                            this.moduleNameMap.put( name, candidate.get() );
                            modules.add( candidate.get() );
                            continue dependency;
                        }
                    } catch ( LazyInitializationException e ) {
                        // An error has occured during lazy initialization; since only the module
                        // loader is invoked this can only be a parse error.
                        throw new ResolutionException( "Failed to parse a project module.", e.getCause() );
                    }
                }

                // Dependency not found, abort
                throw new ResolutionException( "Cannot resolve module \"" + name + "\"" );
            }

        return Collections.unmodifiableCollection( modules );
    }

    public static Collection<Module> resolveModuleDependencies( final Project project, final Module module )
            throws IllegalArgumentException, ResolutionException {
        try {
            return new Resolver( project, module ).resolveModuleDependencies();
        } catch ( IllegalStateException e ) {
            throw new IllegalArgumentException( e.getMessage() );
        }
    }
}
