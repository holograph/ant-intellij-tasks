package com.tomergabel.build.intellij.model;

import java.util.Collection;

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
    public final Collection<String> resolveClasspath( final ModuleResolver resolver, final boolean includeSources,
                                                      final boolean includeTests )
            throws ResolutionException {
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
}
