package com.tomergabel.build.intellij.model;

import com.tomergabel.util.UriUtils;

import java.util.*;

public class ModuleResolver extends PropertyResolver {
    private final Module module;
    private final ProjectResolver projectResolver;

    public ModuleResolver( final Project project, final Module module ) throws ResolutionException {
        this( project == null ? null : new ProjectResolver( project ), module );
    }

    public ModuleResolver( final Module module ) throws ResolutionException {
        this( (ProjectResolver) null, module );
    }

    public ModuleResolver( final ProjectResolver projectResolver, final Module module ) throws ResolutionException {
        super( projectResolver );
        if ( module == null )
            throw new IllegalArgumentException( "The module cannot be null." );

        this.projectResolver = projectResolver;
        this.module = module;
    }

    public Module getModule() {
        return this.module;
    }

    public String resolveModuleOutput() throws ResolutionException {
        // Assert that a module has been specified
        if ( this.module == null )
            throw new ResolutionException( "Cannot resolve module dependencies, module not specified" );

        if ( this.module.getOutputUrl() != null )
            return UriUtils.getPath( resolveUriString( this.module.getOutputUrl() ) );
        if ( this.projectResolver == null )
            throw new ResolutionException(
                    "Module does not specify an output directory and project was not specified." );
        if ( this.projectResolver.getProject().getOutputUrl() == null )
            throw new ResolutionException(
                    "Module does not specify an output directory and the project does not specify a default." );
        return UriUtils.getPath( resolveUriString( this.projectResolver.getProject().getOutputUrl() ) );
    }

    public Collection<String> resolveModuleClasspath() throws ResolutionException {
        final Collection<String> classpath = new HashSet<String>();

        // Iterate all module dependencies. Aggregate each dependency's classpath
        // and output directory.
        for ( final Module dependency : resolveModuleDependencies() ) {
            if ( this.projectResolver == null )
                throw new ResolutionException( "Cannot resolve module dependencies, project not specified" );

            final ModuleResolver dependencyResolver = this.projectResolver.getModuleResolver( dependency );
            classpath.addAll( dependencyResolver.resolveModuleClasspath() );
            classpath.add( dependencyResolver.resolveModuleOutput() );
        }

        // Iterate all library dependencies add add them to the classpath.
        classpath.addAll( resolveLibraryDependencies() );

        return Collections.unmodifiableCollection( classpath );
    }

    public Collection<String> resolveLibraryDependencies() throws ResolutionException {
        // Assert that a module has been specified
        if ( this.module == null )
            throw new ResolutionException( "Cannot resolve module dependencies, module not specified" );

        // Iterate dependencies and process library dependencies
        final Collection<String> dependencies = new HashSet<String>();
        for ( final Dependency dependency : this.module.getDependencies() )
            if ( dependency instanceof LibraryDependency ) {
                final LibraryDependency library = (LibraryDependency) dependency;

                // Resolve URI according to library level
                switch ( library.getLevel() ) {
                    case MODULE:
                        // TODO add support for module dependencies
                        throw new ResolutionException(
                                "Module library dependencies not supported (dependee=\"" + library.getName() + "\")." );

                    case PROJECT:
                        // Assert that a project was specified (we're dealing with project-level libraries)
                        if ( this.projectResolver == null )
                            throw new ResolutionException( "Project not specified but module contains " +
                                    "project library dependencies (dependee=" + library.getName() + "\")." );

                        // Ensure that the project contains the required library
                        final Map<String, Library> libraries = this.projectResolver.getProject().getLibraries();
                        if ( !libraries.containsKey( library.getName() ) )
                            throw new ResolutionException(
                                    "Cannot resolve dependency on project library \"" + library.getName() + "\"" );

                        // Resolve the library and add it to the dependency list
                        for ( final String uri : libraries.get( library.getName() ).getClasses() )
                            dependencies.add( UriUtils.getPath( resolveUriString( uri ) ) );
                        break;

                    default:
                        // Safety net (should never happen)
                        throw new ResolutionException( "Unknown library level \"" + library.getLevel() + "\"" );
                }
            }

        return Collections.unmodifiableCollection( dependencies );
    }

    public Collection<Module> resolveModuleDependencies() throws ResolutionException {
        // Iterate dependencies and process module dependencies
        final Collection<Module> modules = new ArrayList<Module>( this.module.getDependencies().size() );
        for ( final Dependency dependency : this.module.getDependencies() )
            if ( dependency instanceof ModuleDependency ) {
                // Assert that a project was specified to resolve module dependencies
                if ( this.projectResolver == null )
                    throw new ResolutionException( "Cannot resolve module dependencies, project not specified" );

                // Look up module in name map
                try {
                    modules.add( this.projectResolver.getModule( ( (ModuleDependency) dependency ).name ) );
                } catch ( ResolutionException e ) {
                    throw new ResolutionException(
                            "Failed to resolve module dependencies for module \"" + this.module.getName() + "\"", e );
                }
            }

        return Collections.unmodifiableCollection( modules );
    }

    @Override
    protected Map<String, String> generatePropertyMap() {
        return this.module.getProperties();
    }
}
