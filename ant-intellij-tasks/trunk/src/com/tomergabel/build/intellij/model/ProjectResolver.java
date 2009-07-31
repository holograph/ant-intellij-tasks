package com.tomergabel.build.intellij.model;

import com.tomergabel.util.*;

import java.net.URI;
import java.util.*;

public class ProjectResolver extends PropertyResolver {
    private final Project project;
    private final Map<URI, Lazy<Module>> moduleDescriptorMap;
    private final Map<String, Lazy<Module>> moduleNameMap;
    private final Map<Lazy<Module>, Lazy<ModuleResolver>> moduleResolverLazyMap;
    private final Map<Module, ModuleResolver> moduleResolverMap;

    public ProjectResolver( final Project project ) throws ResolutionException {
        super( null );
        if ( project == null )
            throw new IllegalArgumentException( "The project cannot be null." );

        this.project = project;
        this.moduleDescriptorMap = new HashMap<URI, Lazy<Module>>();
        this.moduleNameMap = new HashMap<String, Lazy<Module>>();
        this.moduleResolverLazyMap = new HashMap<Lazy<Module>, Lazy<ModuleResolver>>();
        this.moduleResolverMap = new HashMap<Module, ModuleResolver>();

        // Iterate modules and create module maps
        for ( final String moduleUrl : project.getModules() ) {
            // Resolve module descriptor URL and module name
            final URI resolvedDescriptor = resolveUriString( moduleUrl );
            final String fileName = UriUtils.getFilename( resolvedDescriptor );
            if ( !fileName.toLowerCase().endsWith( ".iml" ) )
                throw new ResolutionException( "Module descriptor URI \"" + resolvedDescriptor +
                        "\" does not point to a valid IDEA module file (.iml)" );
            final String moduleName =
                    fileName.lastIndexOf( '.' ) != -1 ? fileName.substring( 0, fileName.lastIndexOf( '.' ) ) : fileName;

            // Create lazy loader
            final Lazy<Module> loader = new Lazy<Module>() {
                @Override
                public Module call() throws Exception {
                    return Module.parse( resolvedDescriptor );
                }
            };

            // Add to module maps
            if ( this.moduleDescriptorMap.put( resolvedDescriptor, loader ) != null )
                throw new ResolutionException(
                        "Module descriptor URI \"" + moduleUrl + "\" resolves to more than one module" );
            if ( this.moduleNameMap.put( moduleName, loader ) != null )
                throw new ResolutionException( "Module name \"" + moduleName + "\" resolves to more than one module" );
            this.moduleResolverLazyMap.put( loader, new Lazy<ModuleResolver>() {
                @Override
                public ModuleResolver call() throws Exception {
                    final ModuleResolver resolver = new ModuleResolver( ProjectResolver.this, loader.get() );
                    ProjectResolver.this.moduleResolverMap.put( loader.get(), resolver );
                    return resolver;
                }
            } );
        }
    }

    private void preloadModules() throws ResolutionException {
        preloadModules( this.moduleDescriptorMap.values() );
    }

    private void preloadModules( final Iterable<Lazy<Module>> modules ) throws ResolutionException {
        for ( final Lazy<Module> module : modules ) {
            try {
                // Load module
                module.get();

                // Create module resolver
                this.moduleResolverLazyMap.get( module ).get();
            } catch ( LazyInitializationException e ) {
                // An error has occured during lazy initialization; since only the module
                // loader is invoked this can only be a parse error.
                throw new ResolutionException( e );
            }
        }
    }

    @Override
    protected Map<String, String> generatePropertyMap() {
        return this.project.getProperties();
    }

    public Collection<Module> resolveModuleBuildOrder() throws ResolutionException {
        preloadModules();
        return resolveModuleBuildOrder( this.moduleResolverMap.keySet() );
    }

    public Collection<Module> resolveModuleBuildOrderByName( final Iterable<String> moduleNames )
            throws ResolutionException {
        final Collection<Module> modules = new HashSet<Module>();
        for ( final String name : moduleNames )
            modules.add( getModule( name ) );
        return resolveModuleBuildOrder( modules );
    }

    public Collection<Module> resolveModuleBuildOrder( final Iterable<Module> modules ) throws ResolutionException {
        // Assert that a project has been specified
        if ( this.project == null )
            throw new ResolutionException( "Cannot resolve module build order, project not specified" );

        // Iterate descriptor map. We'll have to lazy-load each module to extract its dependencies
        final Map<Module, Integer> nesting = new HashMap<Module, Integer>();
        processModuleDependencyTree( nesting, new ArrayDeque<Module>(), modules );

        // Resolve according to dependency tree depth and render into priority queue
        final PriorityQueue<Module> pq = new PriorityQueue<Module>( nesting.size(), new Comparator<Module>() {
            @Override
            public int compare( final Module o1, final Module o2 ) {
                if ( o1 == null || o2 == null )
                    // Safety net, should never happen
                    throw new IllegalArgumentException( "Null module encountered?" );
                return nesting.get( o2 ) - nesting.get( o1 );
            }
        } );
        pq.addAll( nesting.keySet() );

        // Return queue
        return Collections.unmodifiableCollection( pq );
    }

    private void processModuleDependencyTree( final Map<Module, Integer> nesting, final Deque<Module> ancestry,
                                              final Iterable<Module> modules ) throws ResolutionException {
        final Module parent = ancestry.peekLast();

        for ( final Module module : modules ) {
            // Verify no circular paths
            if ( ancestry.contains( module ) )
                throw new ResolutionException(
                        String.format( "Circular dependency detected between modules \"%s\" and \"%s\"",
                                module.getName(), parent.getName() ) );

            // Update nesting
            nesting.put( module,
                    Math.max( nesting.containsKey( module ) ? nesting.get( module ) : 1, ancestry.size() + 1 ) );

            // Recurse
            final Deque<Module> newAncestry = new ArrayDeque<Module>( ancestry );
            newAncestry.add( module );
            processModuleDependencyTree( nesting, newAncestry,
                    getModuleResolver( module ).resolveModuleDependencies() );
        }
    }

    public ModuleResolver getModuleResolver( final String moduleName )
            throws IllegalArgumentException, ResolutionException {
        if ( moduleName == null )
            throw new IllegalArgumentException( "The module name cannot be null." );
        final Lazy<Module> m = this.moduleNameMap.get( moduleName );
        if ( m == null )
            throw new IllegalArgumentException( "No module \"" + moduleName + "\" found in project." );
        try {
            return this.moduleResolverLazyMap.get( m ).get();
        } catch ( LazyInitializationException e ) {
            throw new ResolutionException( e.getCause() );
        }
    }

    public Module getModule( final String moduleName ) throws IllegalArgumentException, ResolutionException {
        if ( moduleName == null )
            throw new IllegalArgumentException( "The module name cannot be null." );
        final Lazy<Module> m = this.moduleNameMap.get( moduleName );
        if ( m == null )
            throw new ResolutionException( "No module \"" + moduleName + "\" found in project." );
        try {
            return m.get();
        } catch ( LazyInitializationException e ) {
            throw new ResolutionException( e.getCause() );
        }
    }

    public ModuleResolver getModuleResolver( final Module module )
            throws IllegalArgumentException, ResolutionException {
        if ( module == null )
            throw new IllegalArgumentException( "The module cannot be null." );
        final ModuleResolver resolver = this.moduleResolverMap.get( module );
        return resolver != null ? resolver : getModuleResolver( module.getName() );
    }

    public Project getProject() {
        return this.project;
    }
}
