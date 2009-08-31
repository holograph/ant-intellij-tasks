package com.tomergabel.build.intellij.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class ModuleDependency implements Dependency {
    public final String name;

    public ModuleDependency( final String name ) throws IllegalArgumentException {
        super();
        if ( name == null )
            throw new IllegalArgumentException( "Module name cannot be null." );
        this.name = name;
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        final ModuleDependency that = (ModuleDependency) o;

        return !( this.name != null ? !name.equals( that.name ) : that.name != null );

    }

    @Override
    public int hashCode() {
        return this.name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "module:" + this.name;
    }

    @Override
    public Collection<String> resolveClasspath( final ModuleResolver resolver ) throws ResolutionException {
        // Resolve the depenendee against the project
        final ProjectResolver project = resolver.getProjectResolver();
        if ( project == null )
            throw new ResolutionException(
                    "Cannot resolve module \"" + this.name + "\" against module \"" + resolver.getModule().getName() +
                            "\" because a project was not specified." );
        final ModuleResolver dependee = project.getModuleResolver( this.name );

        // A module dependency's classpath contribution comprises its own classpath, plus its compile output
        final Collection<String> classpath = new HashSet<String>();
        classpath.addAll( dependee.resolveModuleClasspath() );
        classpath.add( dependee.resolveModuleOutputPath( false ) );
        return Collections.unmodifiableCollection( classpath );
    }
}
