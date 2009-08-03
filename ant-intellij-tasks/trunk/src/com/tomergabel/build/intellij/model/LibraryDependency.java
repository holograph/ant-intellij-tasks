package com.tomergabel.build.intellij.model;

import com.tomergabel.util.UriUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Specifies a dependency on a library.
 * <p/>
 * Libraries can be specified on a specific module, on the project itself or globally (i.e. shared between projects on
 * the same workstation). This implementation currently <em>does not support</em> global dependencies.
 */
public abstract class LibraryDependency implements Dependency {
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
//         Global,      // Global dependencies are yet not supported

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
     * Returns the level in which the library is specified.
     *
     * @return The {@link Level} value for this dependency.
     */
    public abstract Level getLevel();

    @Override
    public final Collection<String> resolveClasspath( final ModuleResolver resolver ) throws ResolutionException {
        // Resolve the library
        final Library library = resolveLibrary( resolver );

        // Return the library's resolved classpath
        return library.resolveClasspath( resolver );
    }

    /**
     * Resolves this dependency and returns the corresponding library.
     *
     * @param resolver The module against which this dependency should be resolved.
     * @return The library {@link Library instance}.
     * @throws ResolutionException An error has occurred while resolving the library.
     */
    public abstract Library resolveLibrary( final ModuleResolver resolver ) throws ResolutionException;

//        // First, see if a library was specifically specified. If not we'll have to fallback to a library
//        // container.
//        if ( this.library != null )
//            return this.library;
//
//        // Resolve library container
//        final Map<String, Library> libraries;
//        switch ( this.level ) {
//            case MODULE:
//                libraries = resolver.getModule().getLibraries();
//                break;
//            case PROJECT:
//                final ProjectResolver project = resolver.getProjectResolver();
//                if ( project == null )
//                    throw new ResolutionException( "Cannot resolve project-level library \"" + this.name +
//                            "\" because no project was specified." );
//                libraries = project.getProject().getLibraries();
//                break;
//            default:
//                throw new IllegalStateException( "Unrecognized library level \"" + this.level + "\"" );
//        }
//
//        // Resolve library
//        if ( !libraries.containsKey( this.name ) )
//            throw new ResolutionException(
//                    "Cannot find " + this.level.toString().toLowerCase() + "-level library \"" + this.name + "\"." );
//        return libraries.get( this.name );
}
