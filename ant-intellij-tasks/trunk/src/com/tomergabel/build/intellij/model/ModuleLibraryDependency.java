package com.tomergabel.build.intellij.model;

public class ModuleLibraryDependency extends LibraryDependency {
    private final Library library;

    public ModuleLibraryDependency( final Library library ) {
        super();
        this.library = library;
    }

    @Override
    public Level getLevel() {
        return Level.MODULE;
    }

    @Override
    public Library resolveLibrary( final ModuleResolver resolver ) throws ResolutionException {
        return this.library;
    }

    @SuppressWarnings( { "RedundantIfStatement" } )
    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        final ModuleLibraryDependency that = (ModuleLibraryDependency) o;

        if ( this.library != null ? !library.equals( that.library ) : that.library != null ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return this.library != null ? library.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "module library" +
                ( this.library.getName() != null ? ( " \"" + this.library.getName() + "\"" ) : "" );
    }
}
