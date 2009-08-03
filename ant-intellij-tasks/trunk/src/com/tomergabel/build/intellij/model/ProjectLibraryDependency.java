package com.tomergabel.build.intellij.model;

import java.util.Map;

public class ProjectLibraryDependency extends LibraryDependency {
    private final String name;

    public ProjectLibraryDependency( final String name ) {
        super();
        this.name = name;
    }

    @Override
    public Level getLevel() {
        return Level.PROJECT;
    }

    @Override
    public Library resolveLibrary( final ModuleResolver resolver ) throws ResolutionException {
        // Resolve project libraries
        final ProjectResolver project = resolver.getProjectResolver();
        if ( project == null )
            throw new ResolutionException( "Cannot resolve project-level library \"" + this.name +
                    "\" because no project was specified." );
        final Map<String, Library> libraries = project.getProject().getLibraries();

        // Resolve dependency
        if ( !libraries.containsKey( this.name ) )
            throw new ResolutionException(
                    "Cannot find project-level library \"" + this.name + "\"." );
        return libraries.get( this.name );
    }

    @SuppressWarnings( { "RedundantIfStatement" } )
    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        final ProjectLibraryDependency that = (ProjectLibraryDependency) o;

        if ( this.name != null ? !name.equals( that.name ) : that.name != null ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return this.name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "project library \"" + this.name + "\"";
    }
}
