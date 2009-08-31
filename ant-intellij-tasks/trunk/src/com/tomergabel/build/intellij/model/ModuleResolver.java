package com.tomergabel.build.intellij.model;

import static com.tomergabel.util.CollectionUtils.filter;
import com.tomergabel.util.UriUtils;

import java.io.File;
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

    public String resolveModuleOutputPath( final boolean test ) throws ResolutionException {
        return resolveModuleOutput( test ).getAbsolutePath();
    }

    public File resolveModuleOutput( final boolean test ) throws ResolutionException {
        // Assert that a module has been specified
        if ( this.module == null )
            throw new ResolutionException( "Cannot resolve module dependencies, module not specified" );

        // First, see if the model defines its own output
        final String moduleOutput = test ? this.module.getTestOutputUrl() : this.module.getOutputUrl();
        if ( moduleOutput != null )
            return UriUtils.getFile( resolveUriString( moduleOutput ) );

        // Generate an output directory from the project
        if ( this.projectResolver == null )
            throw new ResolutionException(
                    "Module does not specify an output directory and project was not specified." );
        if ( this.projectResolver.getProject().getOutputUrl() == null )
            throw new ResolutionException(
                    "Module does not specify an output directory and the project does not specify a default." );

        final File root = UriUtils.getFile( resolveUriString( this.projectResolver.getProject().getOutputUrl() ) );
        return new File( root, ( test ? "test" : "production" ) + File.separator + this.module.getName() );
    }

    public Collection<String> resolveModuleClasspath( final boolean includeSources, final boolean includeTests )
            throws ResolutionException {
        final Collection<String> classpath = new HashSet<String>();
        for ( final Dependency dependency : this.module.getDependencies() )
            classpath.addAll( dependency.resolveClasspath( this, includeSources, includeTests ) );
        if ( includeSources )
            classpath.add( resolveModuleOutputPath( false ) );
        if ( includeTests )
            classpath.add( resolveModuleOutputPath( true ) );
        return Collections.unmodifiableCollection( classpath );
    }

    public Collection<String> resolveLibraryDependencies() throws ResolutionException {
        // Iterate dependencies and process library dependencies
        final Collection<String> dependencies = new HashSet<String>();
        for ( final LibraryDependency library : filter( this.getModule().getDependencies(), LibraryDependency.class ) )
            dependencies.addAll( library.resolveClasspath( this, true, true ) );
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
