package com.tomergabel.build.intellij.model;

public class LibraryDependency extends Dependency {
    public enum Level {
        PROJECT,
        MODULE,;
        // Global,      // Global dependencies are not supported

        public static Level parse( final String level ) throws IllegalArgumentException {
            return Level.valueOf( level.toUpperCase() );
        }
    }

    public final Level level;
    public final String name;

    public LibraryDependency( Level level, String name ) {
        this.level = level;
        this.name = name;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        LibraryDependency that = (LibraryDependency) o;

        return !( name != null ? !name.equals( that.name ) : that.name != null ) && level == that.level;

    }

    @Override
    public int hashCode() {
        int result = level != null ? level.hashCode() : 0;
        result = 31 * result + ( name != null ? name.hashCode() : 0 );
        return result;
    }

    @Override
    public String toString() {
        return "library:" + this.name + " (" + this.level.toString().toLowerCase() + ")";
    }
}
