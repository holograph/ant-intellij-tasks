package com.tomergabel.build.intellij.model;

import com.tomergabel.util.UriUtils;
import static com.tomergabel.util.CollectionUtils.filter;

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
        for ( final Dependency dependency : this.module.getDependencies() )
            classpath.addAll( dependency.resolveClasspath( this ) );
        return Collections.unmodifiableCollection( classpath );
    }

    public Collection<String> resolveLibraryDependencies() throws ResolutionException {
        // Iterate dependencies and process library dependencies
        final Collection<String> dependencies = new HashSet<String>();
        for ( final LibraryDependency library : filter( this.getModule().getDependencies(), LibraryDependency.class ) )
            dependencies.addAll( library.resolveClasspath( this ) );
        return Collections.unmodifiableCollection( dependencies );
    }

    public Collection<Module> resolveModuleDependencies() throws ResolutionException {
        // Iterate dependencies and process module dependencies
        final Collection<Module> modules = new ArrayList<Module>( this.module.getDependencies().size() );
        for ( final ModuleDependency dependency : filter( this.module.getDependencies(), ModuleDependency.class ) ) {
            // Assert that a project was specified to resolve module dependencies
            if ( this.projectResolver == null )
                throw new ResolutionException( "Cannot resolve module dependencies, project not specified" );

            // Look up module in name map
            try {
                modules.add( this.projectResolver.getModule( dependency.name ) );
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

    public ProjectResolver getProjectResolver() {
        return this.projectResolver;
    }
}
