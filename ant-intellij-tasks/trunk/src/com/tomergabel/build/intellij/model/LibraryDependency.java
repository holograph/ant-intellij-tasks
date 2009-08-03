package com.tomergabel.build.intellij.model;

import com.tomergabel.util.UriUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

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
    Collection<String> resolveClasspath( final ModuleResolver resolver ) throws ResolutionException {
        // Resolve library container
        final Map<String, Library> libraries;
        switch ( this.level ) {
            case MODULE:
                libraries = resolver.getModule().getLibraries();
                break;
            case PROJECT:
                final ProjectResolver project = resolver.getProjectResolver();
                if ( project == null )
                    throw new ResolutionException( "Cannot resolve project-level library \"" + this.name +
                            "\" because no project was specified." );
                libraries = project.getProject().getLibraries();
                break;
            default:
                throw new IllegalStateException( "Unrecognized library level \"" + this.level + "\"" );
        }

        // Resolve library
        if ( !libraries.containsKey( this.name ) )
            throw new ResolutionException(
                    "Cannot find " + this.level.toString().toLowerCase() + "-level library \"" + this.name + "\"." );

        // Resolve the library and add it to the dependency list
        final Collection<String> classpath = new HashSet<String>();
        for ( final String uri : libraries.get( this.name ).getClasses() )
            classpath.add( UriUtils.getPath( resolver.resolveUriString( uri ) ) );
        return Collections.unmodifiableCollection( classpath );
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
