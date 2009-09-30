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

package com.tomergabel.build.intellij.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.FileSet;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;

@Ignore( "Pending completion" )
public class PackageModuleJarTaskTests {
    private PackageModuleJarTask task;

    static class TestableJar extends Jar {
        public FileSet list() {
            return this.getImplicitFileSet();
        }
    }

    @Test
    public void execute_ModuleNotSpecified_BuildExceptionIsThrown() {
        try {
            task.execute();
            fail( "Module was not specified, BuildException expected." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void createJarTask_SelfContainedModuleSpecified_JarCreatedCorrectly() {
        // TODO
    }

    @Test
    public void createJarTask_DependentModuleSpecified_JarTaskCreatedSuccessfully() {
        // TODO
    }
}
