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

import static com.tomergabel.build.intellij.model.MockModel.Modules.selfContained;
import com.tomergabel.util.LazyInitializationException;
import org.apache.tools.ant.BuildException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class ModuleConditionBaseTests {
    private ModuleConditionBase condition;

    @Before
    public void setup() {
        this.condition = new ModuleConditionBase() {
            @Override
            protected boolean evaluate() {
                return false;
            }
        };
    }

    @Test
    public void module_ModuleNotSpecified_BuildExceptionIsThrown() {
        try {
            this.condition.module();
            fail( "Module not specified, BuildException expected." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void moduleResolver_ModuleNotSpecified_BuildExceptionIsThrown() {
        try {
            this.condition.moduleResolver();
            fail( "Module not specified, BuildException expected." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void setModule_AfterEvaluation_IllegalStateExceptionIsThrown() throws LazyInitializationException {
        try {
            this.condition.eval();
            this.condition.setModule( selfContained.get() );
            fail( "Condition already evaluated, IllegalStateException expected." );
        } catch ( IllegalStateException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void setModuleDescriptor_AfterEvaluation_IllegalStateExceptionIsThrown() throws LazyInitializationException {
        try {
            this.condition.eval();
            this.condition.setModuleDescriptor( selfContained.get().getModuleDescriptor() );
            fail( "Condition already evaluated, IllegalStateException expected." );
        } catch ( IllegalStateException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void setModuleFile_AfterEvaluation_IllegalStateExceptionIsThrown() throws LazyInitializationException {
        try {
            this.condition.eval();
            this.condition.setModuleFile( new File( selfContained.get().getModuleDescriptor() ) );
            fail( "Condition already evaluated, IllegalStateException expected." );
        } catch ( IllegalStateException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void module_ValidModuleSpecified_ModuleReturned() throws LazyInitializationException {
        this.condition.setModule( selfContained.get() );
        assertEquals( "Incorrect module returned.", selfContained.get(), this.condition.module() );
    }

    @Test
    public void module_ValidModuleFileSpecified_ModuleReturned() throws LazyInitializationException {
        this.condition.setModuleFile( new File( selfContained.get().getModuleDescriptor() ) );
        assertEquals( "Incorrect module returned.", selfContained.get(), this.condition.module() );
    }

    @Test
    public void module_ValidModuleDescriptorSpecified_ModuleReturned() throws LazyInitializationException {
        this.condition.setModuleDescriptor( selfContained.get().getModuleDescriptor() );
        assertEquals( "Incorrect module returned.", selfContained.get(), this.condition.module() );
    }

    @Test
    public void module_InvalidModuleSpecified_BuildExceptionIsThrown() throws LazyInitializationException {
        this.condition.setModule( selfContained.get() );
        assertEquals( "Incorrect module returned.", selfContained.get(), this.condition.module() );
    }
}
