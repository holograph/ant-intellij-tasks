package com.tomergabel.build.intellij.model;

/**
 * Specifies a dependency on a library.
 * <p/>
 * Libraries can be specified on a specific module, on the project itself or globally (i.e. shared between projects on
 * the same workstation). This implementation currently <em>does not support</em> global dependencies.
 */
public class LibraryDependency extends Dependency {
    /**
     * The level in which the library is specified.
     */
    public enum Level {
        /**
         * The library is specified on the project.
         */
        PROJECT,
        /**
         * The library is specified on a module.
         */
        MODULE;
        // Global,      // Global dependencies are yet not supported

        /**
         * Parses the specified level string.
         *
         * @param level The dependency level. The parsing is case-insensitive.
         * @return The {@link Level} value represented by the string.
         * @throws IllegalArgumentException The specified level string is unknown or not supported.
         */
        public static Level parse( final String level ) throws IllegalArgumentException {
            return Level.valueOf( level.toUpperCase() );
        }
    }

    /**
     * The level in which the library is specified.
     */
    protected final Level level;
    /**
     * The library name.
     */
    protected final String name;

    /**
     * Returns the level in which the library is specified.
     *
     * @return The {@link Level} value for this dependency.
     */
    public Level getLevel() {
        return this.level;
    }

    /**
     * Returns the library name for this dependency,
     *
     * @return The library name for this dependency,
     */
    public String getName() {
        return this.name;
    }

    /**
     * Creates and returns a new instance of {@link com.tomergabel.build.intellij.model.LibraryDependency}.
     *
     * @param level The level in which the library is specified.
     * @param name  The library name for this dependency.
     * @throws IllegalArgumentException <ul><li>The level cannot be null.</li><li>The library name cannot be
     *                                  null.</li></ul>
     */
    public LibraryDependency( final Level level, final String name ) throws IllegalArgumentException {
        super();
        if ( level == null )
            throw new IllegalArgumentException( "The level cannot be null." );
        if ( name == null )
            throw new IllegalArgumentException( "The library name cannot be null." );
        this.level = level;
        this.name = name;
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        final LibraryDependency that = (LibraryDependency) o;
        return !( this.name != null ? !this.name.equals( that.name ) : that.name != null ) && this.level == that.level;

    }

    @Override
    public int hashCode() {
        int result = this.level != null ? this.level.hashCode() : 0;
        result = 31 * result + ( this.name != null ? this.name.hashCode() : 0 );
        return result;
    }

    @Override
    public String toString() {
        return "library:" + this.name + " (" + this.level.toString().toLowerCase() + ")";
    }
}
