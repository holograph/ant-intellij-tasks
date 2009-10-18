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

package com.tomergabel.build.intellij.ant.prototype;

import com.tomergabel.build.intellij.model.Project;

import java.io.File;
import java.net.URI;

/**
 * Prototype for Ant-facing APIs (tasks, conditions) that take an input project. To maintain a consitent API, all
 * Ant-facing APIs that work on projects should extend this interface.
 * <p/>
 * I wish Java had mixins.
 */
public interface ProjectReceiver {
    /**
     * Sets the project.
     *
     * @param project The project.
     * @throws IllegalArgumentException The project cannot be null.
     */
    void setProject( final Project project ) throws IllegalArgumentException;

    /**
     * Sets a project file (.ipr).
     *
     * @param projectFile The path to the project file.
     * @throws IllegalArgumentException The project file cannot be null.
     */
    void setProjectFile( final File projectFile ) throws IllegalArgumentException;

    /**
     * Sets a project decsriptor (.ipr) URI.
     *
     * @param projectDescriptor The project decsriptor URI.
     * @throws IllegalArgumentException The URI cannot be null.
     */
    void setProjectDescriptor( final URI projectDescriptor ) throws IllegalArgumentException;
}
