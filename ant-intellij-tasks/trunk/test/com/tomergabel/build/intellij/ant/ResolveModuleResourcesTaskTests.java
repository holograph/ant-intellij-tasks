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
import static com.tomergabel.util.TestUtils.assertSetEquality;
import static junit.framework.Assert.assertNotNull;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

@SuppressWarnings( { "ConstantConditions" } )
public class ResolveModuleResourcesTaskTests extends AntTestBase {
    ResolveModuleResourcesTask task = new ResolveModuleResourcesTask();

    public ResolveModuleResourcesTaskTests() throws URISyntaxException, IOException {
        super();
    }

    @Before
    public void setup() {
        this.task = new ResolveModuleResourcesTask();
        this.task.setProject( this.project );
    }

    @Test
    public void execute_ModuleNotSpecified_ThrowsBuildException() {
        try {
            task.execute();
            fail( "Module not specified, BuildException expected." );
        } catch ( BuildException e ) {
            // Expected, all is well.
        }
    }

    @Test
    public void execute_ModuleNotSpecified_NoFailOnError_NothingHappens() {
        task.setFailonerror( false );
        task.execute();
    }

    @Test
    public void execute_PathIdNotSpecified_ThrowsBuildException() throws Exception {
        task.setModule( MockModel.Modules.selfContained.get() );
        try {
            task.execute();
            fail( "Module not specified, BuildException expected." );
        } catch ( BuildException e ) {
            // Expected, all is well.
        }
    }

    @Test
    public void execute_PathIdNotSpecified_NoFailOnError_NothingHappens() throws Exception {
        task.setFailonerror( false );
        task.setModule( MockModel.Modules.selfContained.get() );
        task.execute();
    }

    @Test
    public void execute_NoProjectSpecified_EmptyPathGenerated() throws Exception {
        task.setPathId( "test-path" );
        task.setModuleDescriptor( this.getClass().getResource( "resources-test.iml" ).toURI() );
        task.execute();

        // Verify that the path has been generated
        final Object reference = project.getReference( "test-path" );
        assertNotNull( "Path object expected to be empty but was not found in project.", reference );
        assertTrue( "Path object found in project, but is not a valid path.", reference instanceof Path );
        final Path path = (Path) reference;
        assertEquals( "Path object expected to be empty but was not.", 0, path.size() );
    }

    @Test
    public void execute_ModuleAndProjectSpecified_ResourcesResolvedCorrectly() throws Exception {
        task.setPathId( "test-path" );
        task.setModuleDescriptor( this.getClass().getResource( "resources-test.iml" ).toURI() );
        task.setProjectDescriptor( this.getClass().getResource( "resources-test.ipr" ).toURI() );
        task.execute();

        // Verify that the path has been generated
        final Object reference = project.getReference( "test-path" );
        assertNotNull( "Path object expected but was not found in project.", reference );
        assertTrue( "Path object found in project, but is not a valid path.", reference instanceof Path );
        final Path path = (Path) reference;
        assertSetEquality( "Resources not incorrectly resolved.", new String[] {
                new File( this.getClass().getResource( "resources-src/test.gif" ).toURI() ).getAbsolutePath(),
                new File( this.getClass().getResource( "resources-src/test.jpg" ).toURI() ).getAbsolutePath(),
                new File( this.getClass().getResource( "resources-test/test.gif" ).toURI() ).getAbsolutePath()
        }, path.list() );
    }
}
