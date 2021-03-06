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

import com.tomergabel.build.intellij.model.MockModel;
import static com.tomergabel.build.intellij.model.MockModel.Modules.dependantModule;
import static com.tomergabel.build.intellij.model.MockModel.Modules.dependee;
import static junit.framework.Assert.assertNull;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class ResolveModuleDependenciesTaskTests {
    private ResolveModuleDependenciesTask task;
    private Project project;

    @Before
    public void testSetup() {
        this.task = new ResolveModuleDependenciesTask();
        this.task.setProject( this.project = new Project() );
    }

    @Test
    public void testDepdencyResolution_ProjectNotSpecifiedWithNoModuleDependencies_EmptyCollectionReturned()
            throws Exception {
        task.setModule( dependee.get() );
        task.setProperty( "property" );
        task.execute();
        assertEquals( "Depdency list incorrectly parsed.", "", project.getProperty( "property" ) );
    }

    @Test
    public void testDepdencyResolution_ProjectNotSpecifiedWithModuleDependencies_BuildExceptionIsThrown()
            throws Exception {
        task.setModule( dependantModule.get() );
        task.setProperty( "property" );
        try {
            task.execute();
            fail( "Resolution did not fail even though project file was not specified and " +
                    "module dependencies are present." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testDepdencyResolution_ProjectNotSpecifiedWithModuleDependencies_NoFailOnError_NothingHappens()
            throws Exception {
        task.setFailonerror( false );
        task.setModule( dependantModule.get() );
        task.setProperty( "property" );
        task.execute();
        assertNull( "Property was generated despite the error, null expected.", project.getProperty( "property" ) );
    }
    
    @Test
    public void testDepdencyResolution_ProjectSpecifiedWithModuleDependencies_DependenciesResolvedCorrectly()
            throws Exception {
        task.setModule( dependantModule.get() );
        task.setProperty( "property" );
        task.setMode( ResolutionModes.names );
        // TODO descriptors mode tests
        task.setProject( MockModel.Projects.allModules.get() );
        task.execute();
        assertEquals( "Model dependency resolution failed.", dependee.get().getName(),
                project.getProperty( "property" ) );
    }
}
