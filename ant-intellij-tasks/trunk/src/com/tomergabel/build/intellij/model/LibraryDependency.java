/*
	Copyright 2009 Tomer Gabel <tomer@tomergabel.com>

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


	ant-intellij-tasks project (http://code.google.com/p/ant-intellij-tasks/)

	$Id$
*/

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
