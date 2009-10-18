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
import com.tomergabel.util.LazyInitializationException;
import org.apache.tools.ant.BuildException;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.io.IOException;

public class ResolveModuleOutputTaskTests extends AntTestBase {
    ResolveModuleOutputTask task;

    public ResolveModuleOutputTaskTests() throws URISyntaxException, IOException {
        super();
    }

    @Before
    public void testSetup() {
        this.task = new ResolveModuleOutputTask();
        this.task.setProject( this.project );
    }

    @Test
    public void resolveOutputDirectory_ProjectNotSpecified_OutputWithProjectRelativePath_BuildExceptionThrown()
            throws URISyntaxException, LazyInitializationException {
        this.task.setModule( MockModel.Modules.outputProjectRelative.get() );
        try {
            this.task.execute();
            fail( "Resolution did not fail even though project file was not specified and " +
                    "output directory is project-relative." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

//    @Test
//    public void resolveOutputDirectory_ProjectNotSpecified_OutputWithoutProjectRelativePath_PathResolvedCorrectly()
//            throws URISyntaxException {
//        this.task.setModuleDescriptor( ModuleOutputs.moduleRelative );
//        assertEquals( "Output directory resolved incorrectly.",
//                new File( this.task.module().getModuleRoot().resolve( "bin" ) ).getAbsolutePath(),
//                this.task.resolveOutputDirectory() );
//    }
//
//    @Test
//    public void resolveOutputDirectory_ProjectSpecified_OutputWithProjectRelativePath_PathResolvedCorrectly()
//            throws URISyntaxException {
//        this.task.setModuleDescriptor( ModuleOutputs.projectRelative );
//        this.task.setProjectDescriptor( ProjectOutputs.specified );
//        assertEquals( "Output directory resolved incorrectly.",
//                new File( this.task.project().getProjectRoot().resolve( "bin" ) ).getAbsolutePath(),
//                this.task.resolveOutputDirectory() );
//    }
//
//    @Test
//    public void resolveOutputDirectory_ProjectNotSpecified_UnspecifiedModuleOutput_BuildExceptionThrown()
//            throws URISyntaxException {
//        this.task.setModuleDescriptor( ModuleOutputs.unspecified );
//        try {
//            this.task.resolveOutputDirectory();
//            fail( "Resolution did not fail even though project file was not specified and " +
//                    "module does not specify output directory." );
//        } catch ( BuildException e ) {
//            // Expected, all is well
//        }
//    }
//
//    @Test
//    public void resolveOutputDirectory_ProjectWithOutputSpecified_UnspecifiedModuleOutput_FallsBackToProjectOutput()
//            throws URISyntaxException {
//        this.task.setModuleDescriptor( ModuleOutputs.unspecified );
//        this.task.setProjectDescriptor( ProjectOutputs.specified );
//        assertEquals( "Output directory resolved incorrectly.",
//                new File( this.task.project().getProjectRoot().resolve( "out" ) ).getAbsolutePath(),
//                this.task.resolveOutputDirectory() );
//    }
//
//    @Test
//    public void resolveOutputDirectory_ProjectWithNoOutputSpecified_UnspecifiedModuleOutput_BuildExceptionIsThrown()
//            throws URISyntaxException {
//        this.task.setModuleDescriptor( ModuleOutputs.unspecified );
//        this.task.setProjectDescriptor( ProjectOutputs.unspecified );
//        try {
//            this.task.resolveOutputDirectory();
//            fail( "Resolution did not fail even though project does not specify output URL and " +
//                    "module does not specify output directory." );
//        } catch ( BuildException e ) {
//            // Expected, all is well
//        }
//    }
//
//    // TODO failOnError tests
}
