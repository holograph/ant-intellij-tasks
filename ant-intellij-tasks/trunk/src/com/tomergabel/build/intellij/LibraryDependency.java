package com.tomergabel.build.intellij;

class LibraryDependency extends Dependency {
    public enum Scope {
        PROJECT,
        MODULE,;
        // Global,      // Global dependencies are not supported

        public static Scope parse( final String level ) throws IllegalArgumentException {
            return Scope.valueOf( level.toUpperCase() );
        }
    }

    public final Scope scope;
    public final String name;

    public LibraryDependency( Scope scope, String name ) {
        this.scope = scope;
        this.name = name;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        LibraryDependency that = (LibraryDependency) o;

        if ( name != null ? !name.equals( that.name ) : that.name != null ) return false;
        if ( scope != that.scope ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = scope != null ? scope.hashCode() : 0;
        result = 31 * result + ( name != null ? name.hashCode() : 0 );
        return result;
    }
}
