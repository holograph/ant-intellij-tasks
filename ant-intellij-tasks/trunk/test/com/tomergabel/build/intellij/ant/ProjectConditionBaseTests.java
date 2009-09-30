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

import java.io.File;

public class ProjectConditionBaseTests {
    static class TestableCondition extends ProjectConditionBase {
        @Override
        protected boolean evaluate() {
            return false;
        }
    }
    private ProjectConditionBase condition;

    @Before
    public void setup() {
        this.condition = new TestableCondition();
    }

    @Test
    public void assertProjectSpecified_ProjectNotSpecified_BuildExceptionIsThrown() {
        try {
            this.condition.assertProjectSpecified();
            fail( "Project not specified, BuildException expected." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void assertProjectSpecified_ProjectSpecified_NothingHappens() throws LazyInitializationException {
        this.condition.setProject( MockModel.Projects.allModules.get() );
        this.condition.assertProjectSpecified();
    }

    @Test
    public void assertProjectSpecified_ProjectFileSpecified_NothingHappens() throws LazyInitializationException {
        this.condition.setProjectFile( new File( MockModel.Projects.allModules.get().getDescriptor() ) );
        this.condition.assertProjectSpecified();
    }

    @Test
    public void assertProjectSpecified_ProjectDescriptorSpecified_NothingHappens() throws LazyInitializationException {
        this.condition.setProjectDescriptor( MockModel.Projects.allModules.get().getDescriptor() );
        this.condition.assertProjectSpecified();
    }

    @Test
    public void setProject_AfterEvaluation_IllegalStateExceptionIsThrown() throws LazyInitializationException {
        this.condition.eval();
        try {
            this.condition.setProject( MockModel.Projects.allModules.get() );
            fail( "Project specified after condition was evaluated, IllegalStateException expected" );
        } catch ( IllegalStateException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void setProjectFile_AfterEvaluation_IllegalStateExceptionIsThrown() throws LazyInitializationException {
        this.condition.eval();
        try {
            this.condition.setProjectFile( new File( MockModel.Projects.allModules.get().getDescriptor() ) );
            fail( "Project specified after condition was evaluated, IllegalStateException expected" );
        } catch ( IllegalStateException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void setProjectDescriptor_AfterEvaluation_IllegalStateExceptionIsThrown() throws LazyInitializationException {
        this.condition.eval();
        try {
            this.condition.setProjectDescriptor( MockModel.Projects.allModules.get().getDescriptor() );
            fail( "Project specified after condition was evaluated, IllegalStateException expected" );
        } catch ( IllegalStateException e ) {
            // Expected, all is well
        }
    }
}
