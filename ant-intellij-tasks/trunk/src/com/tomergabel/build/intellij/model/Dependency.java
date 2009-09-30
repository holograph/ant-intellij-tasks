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
 * An abstract class providing a common base object for dependencies within an IntelliJ IDEA project.
 */
public interface Dependency {
    /**
     * Resolves the entries this dependency contributes to a dependent module's classpath.
     *
     * @param resolver       The {@link com.tomergabel.build.intellij.model.ModuleResolver module resolver} against
     *                       which to resolve the classpath entries.
     * @param includeSources Selects the source classpath entries for this dependency. If the dependency does not
     *                       differentiate between source and test output, this parameter is ignored.
     * @param includeTests   Selects the test classpath entries for this dependency. If the dependency does not
     *                       differentiate between source and test output, this parameter is ignored.
     * @return The classpath entries resolved against the specified module.
     * @throws IllegalArgumentException The module resolver cannot be null.
     * @throws ResolutionException      An error has occurred while resolving the classpath.
     */
    Collection<String> resolveClasspath( final ModuleResolver resolver, final boolean includeSources,
                                         final boolean includeTests )
            throws IllegalArgumentException, ResolutionException;
}
