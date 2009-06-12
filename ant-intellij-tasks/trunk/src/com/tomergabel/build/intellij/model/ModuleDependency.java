package com.tomergabel.build.intellij.model;

public class ModuleDependency extends Dependency {
    public final String name;

    public ModuleDependency( String name ) throws IllegalArgumentException {
        if ( name == null )
            throw new IllegalArgumentException( "Module name cannot be null." );
        this.name = name;
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        final ModuleDependency that = (ModuleDependency) o;

        return !( name != null ? !name.equals( that.name ) : that.name != null );

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "module:" + this.name;
    }
}
