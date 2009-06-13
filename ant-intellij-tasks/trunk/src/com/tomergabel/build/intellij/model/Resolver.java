package com.tomergabel.build.intellij.model;

import com.tomergabel.util.Lazy;
import com.tomergabel.util.LazyInitializationException;
import com.tomergabel.util.UriUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

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
                final URI resolvedDescriptor = resolveUriString( moduleUrl );
                this.moduleDescriptorMap.put( resolvedDescriptor, new Lazy<Module>() {
                    @Override
                    public Module call() throws Exception {
                        return Module.parse( resolvedDescriptor );
                    }
                } );
            }

            // Override current module with preloaded instance
            if ( module != null ) {
                this.moduleDescriptorMap.put( module.getDescriptor(), Lazy.from( module ) );
                this.moduleNameMap.put( module.getName(), module );
            }
        } else {
            this.moduleNameMap = null;
            this.moduleDescriptorMap = null;
        }

        // Override project properties with module properties, if applicable
        if ( module != null )
            this.properties.putAll( module.getProperties() );
    }

    public URI resolveUriString( String string ) throws IllegalArgumentException, ResolutionException {
        if ( string == null )
            return null;

        // Expand embedded properties
        final String expandedString = expandProperties( string );
        final URI expandedUri;
        try {
            expandedUri = new URI( expandedString );
        } catch ( URISyntaxException e ) {
            throw new ResolutionException( "Invalid expanded URI generated: " + expandedString, e );
        }

        // Normalize URI
        if ( expandedUri.getScheme().equals( "file" ) )
            return expandedUri;
        else if ( expandedUri.getScheme().equals( "jar" ) ) {
            // Construct new URI: replace schema with "file" and strip trailing !/
            final String newUri = expandedUri.toString().replace( "jar:", "file:" );
            try {
                return new URI( newUri.endsWith( "!/" ) ? newUri.substring( 0, newUri.length() - 2 ) : newUri );
            } catch ( URISyntaxException e ) {
                throw new ResolutionException( "JAR URI resolution failed", e );
            }
        } else throw new ResolutionException( "Unrecognized URI scheme \"" + expandedUri.getScheme() + "\"" );
    }

    private String expandProperties( final String string ) throws ResolutionException {
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
        return sb.toString();
    }

    public static URI resolveUri( Project project, Module module, String string )
            throws IllegalArgumentException, ResolutionException {
        return new Resolver( project, module ).resolveUriString( string );
    }

    public Collection<Module> resolveModuleDependencies() throws ResolutionException {
        // Assert that a module has been specified
        if ( this.module == null )
            throw new ResolutionException( "Cannot resolve module dependencies, module not specified" );

        // Iterate dependencies and process module dependencies
        final Collection<Module> modules = new ArrayList<Module>( this.module.getDependencies().size() );
        dependency:
        for ( Dependency dependency : this.module.getDependencies() )
            if ( dependency instanceof ModuleDependency ) {
                // Assert that a project was specified to resolve module dependencies
                if ( this.project == null )
                    throw new ResolutionException( "Cannot resolve module dependencies, project not specified" );

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
            throws ResolutionException {
        if ( module == null )
            throw new IllegalArgumentException( "The module cannot be null." );

        return new Resolver( project, module ).resolveModuleDependencies();
    }

    public Collection<String> resolveLibraryDependencies() throws ResolutionException {
        // Assert that a module has been specified
        if ( this.module == null )
            throw new ResolutionException( "Cannot resolve module dependencies, module not specified" );

        // Iterate dependencies and process library dependencies
        final Collection<String> dependencies = new HashSet<String>();
        for ( Dependency dependency : this.module.getDependencies() )
            if ( dependency instanceof LibraryDependency ) {
                final LibraryDependency library = (LibraryDependency) dependency;

                // Resolve URI according to library level
                switch ( library.level ) {
                    case MODULE:
                        // TODO add support for module dependencies
                        throw new ResolutionException(
                                "Module library dependencies not supported (dependee=\"" + library.name + "\")." );

                    case PROJECT:
                        // Assert that a project was specified (we're dealing with project-level libraries)
                        if ( this.project == null )
                            throw new ResolutionException( "Project not specified but module contains " +
                                    "project library dependencies (dependee=" + library.name + "\")." );

                        // Ensure that the project contains the required library
                        final Map<String, Project.Library> libraries = this.project.getLibraries();
                        if ( !libraries.containsKey( library.name ) )
                            throw new ResolutionException(
                                    "Cannot resolve dependency on project library \"" + library.name + "\"" );

                        // Resolve the library and add it to the dependency list
                        for ( String uri : libraries.get( library.name ).getClasses() )
                            dependencies.add( UriUtils.getPath( resolveUriString( uri ) ) );
                        break;

                    default:
                        // Safety net (should never happen)
                        throw new ResolutionException( "Unknown library level \"" + library.level + "\"" );
                }
            }

        return Collections.unmodifiableCollection( dependencies );
    }

    public static Collection<String> resolveLibraryDependencies( Project project, Module module )
            throws ResolutionException {
        if ( module == null )
            throw new IllegalArgumentException( "The module cannot be null." );

        return new Resolver( project, module ).resolveLibraryDependencies();
    }

    public Collection<String> resolveClasspath() throws ResolutionException {
        final Collection<String> classpath = new HashSet<String>();

        // Iterate all module dependencies. Aggregate each dependency's classpath
        // and output directory.
        for ( Module dependency : resolveModuleDependencies() ) {
            final Resolver dependencyResolver = new Resolver( this.project, dependency );
            classpath.addAll( dependencyResolver.resolveClasspath() );
            classpath.add( UriUtils.getPath( dependencyResolver.resolveUriString( dependency.getOutputUrl() ) ) );
        }

        // Iterate all library dependencies add add them to the classpath. 
        classpath.addAll( resolveLibraryDependencies() );

        return Collections.unmodifiableCollection( classpath );
    }

    public static Collection<String> resolveClasspath( Project project, Module module )
            throws IllegalArgumentException, ResolutionException {
        if ( module == null )
            throw new IllegalArgumentException( "The module cannot be null." );

        return new Resolver( project, module ).resolveClasspath();
    }
}
