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

import com.tomergabel.build.intellij.model.Module;

import java.io.File;
import java.net.URI;

/**
 * Prototype for Ant-facing APIs (tasks, conditions) that take an input module. To maintain a consitent API, all
 * Ant-facing APIs that work on modules should extend this interface.
 * <p/>
 * I wish Java had mixins.
 */
public interface ModuleReceiver extends ProjectReceiver {
    /**
     * Sets an input module.
     *
     * @param module The input module.
     * @throws IllegalArgumentException The module cannot be null.
     */
    void setModule( final Module module ) throws IllegalArgumentException;

    /**
     * Sets a module file (.iml).
     *
     * @param moduleFile The path to the module file.
     * @throws IllegalArgumentException The module file cannot be null.
     */
    void setModuleFile( final File moduleFile ) throws IllegalArgumentException;

    /**
     * Sets a descriptor (.iml) URI.
     *
     * @param moduleDescriptor The module descriptor URI.
     * @throws IllegalArgumentException The URI cannot be null.
     */
    void setModuleDescriptor( final URI moduleDescriptor ) throws IllegalArgumentException;

    /**
     * Sets an input module by name. If the 'moduleName' attribute is used a project must be set as well.
     *
     * @param moduleName The module name.
     * @throws IllegalArgumentException The module name cannot be null.
     */
    void setModuleName( final String moduleName ) throws IllegalArgumentException;
}
