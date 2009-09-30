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
import static com.tomergabel.build.intellij.model.MockModel.Modules.*;
import com.tomergabel.build.intellij.model.Module;
import static com.tomergabel.util.CollectionUtils.join;
import static com.tomergabel.util.CollectionUtils.map;
import com.tomergabel.util.LazyInitializationException;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

public class ResolveBuildOrderTaskTests {
    ResolveBuildOrderTask task;
    Project project;

    static Collection<Module> buildOrder;
    static String nameBuildOrder;
    static String descriptorBuildOrder;

    @BeforeClass
    public static void fixtureSetup() throws LazyInitializationException {
        buildOrder = Arrays
                .asList( buildOrderTestD.get(), buildOrderTestC.get(), buildOrderTestB.get(), buildOrderTestA.get() );
        nameBuildOrder = join( map( buildOrder, ResolutionModes.names.mapper ) );
        descriptorBuildOrder = join( map( buildOrder, ResolutionModes.descriptors.mapper ) );
    }

    @Before
    public void testSetup() {
        this.task = new ResolveBuildOrderTask();
        this.project = new Project();
        task.setProject( this.project );
    }

    @Test
    public void execute_NoProjectSpecified_BuildExceptionIsThrown() {
        try {
            task.setProperty( "property" );
            task.execute();
            fail( "Project not specified but no exception was thrown." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void execute_NoProjectSpecified_NoFailOnError_NothingHappens() {
        task.setProperty( "property" );
        task.setFailonerror( false );
        task.execute();
        assertNull( "Property generated, no such behavior expected.", project.getProperty( "property" ) );
    }

    @Test
    public void execute_NoPropertySpecified_BuildExceptionIsThrown() throws LazyInitializationException {
        try {
            task.setProject( MockModel.Projects.buildOrderTest.get() );
            task.execute();
            fail( "Property not specified but no exception was thrown." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void execute_NoPropertySpecified_NoFailOnError_NothingHappens() throws LazyInitializationException {
        task.setProject( MockModel.Projects.buildOrderTest.get() );
        task.setFailonerror( false );
        task.execute();
        assertNull( "Property generated, no such behavior expected.", project.getProperty( "property" ) );
    }

    @Test
    public void execute_ProjectProperty_InputUnspecified_NameOutput_PropertyGeneratedCorrectly()
            throws LazyInitializationException {
        task.setProject( MockModel.Projects.buildOrderTest.get() );
        task.setProperty( "property" );
        task.setOutputMode( ResolutionModes.names );
        task.execute();
        final String property = project.getProperty( "property" );
        assertNotNull( "Property was not generated.", property );
        assertEquals( "Build order incorrectly resolved.", nameBuildOrder, property );
    }

    @Test
    public void execute_ProjectProperty_InputUnspecified_DescriptorOutput_PropertyGeneratedCorrectly()
            throws LazyInitializationException {
        task.setProject( MockModel.Projects.buildOrderTest.get() );
        task.setProperty( "property" );
        task.setOutputMode( ResolutionModes.descriptors );
        task.execute();
        final String property = project.getProperty( "property" );
        assertNotNull( "Property was not generated.", property );
        assertEquals( "Build order incorrectly resolved.", descriptorBuildOrder, property );
    }

    @Test
    public void execute_ProjectProperty_InputUnspecified_OutputUnspecified_PropertyGeneratedCorrectly()
            throws LazyInitializationException {
        task.setProject( MockModel.Projects.buildOrderTest.get() );
        task.setProperty( "property" );
        task.execute();
        final String property = project.getProperty( "property" );
        assertNotNull( "Property was not generated.", property );
        assertEquals( "Build order incorrectly resolved.", nameBuildOrder, property );
    }

    @Test
    public void execute_ProjectProperty_NameInput_NameOutput_PropertyGeneratedCorrectly()
            throws LazyInitializationException {
        task.setProject( MockModel.Projects.buildOrderTest.get() );
        task.setProperty( "property" );
        task.setInputMode( ResolutionModes.names );
        task.setOutputMode( ResolutionModes.names );
        task.execute();
        final String property = project.getProperty( "property" );
        assertNotNull( "Property was not generated.", property );
        assertEquals( "Build order incorrectly resolved.", nameBuildOrder, property );
    }

    @Test
    public void execute_ProjectProperty_NamesInput_DescriptorOutput_PropertyGeneratedCorrectly()
            throws LazyInitializationException {
        task.setProject( MockModel.Projects.buildOrderTest.get() );
        task.setProperty( "property" );
        task.setInputMode( ResolutionModes.names );
        task.setOutputMode( ResolutionModes.descriptors );
        task.execute();
        final String property = project.getProperty( "property" );
        assertNotNull( "Property was not generated.", property );
        assertEquals( "Build order incorrectly resolved.", descriptorBuildOrder, property );
    }

    @Test
    public void execute_ProjectProperty_NamesInput_OutputUnspecified_PropertyGeneratedCorrectly()
            throws LazyInitializationException {
        task.setProject( MockModel.Projects.buildOrderTest.get() );
        task.setProperty( "property" );
        task.setInputMode( ResolutionModes.names );
        task.execute();
        final String property = project.getProperty( "property" );
        assertNotNull( "Property was not generated.", property );
        assertEquals( "Build order incorrectly resolved.", nameBuildOrder, property );
    }

    @Test
    public void execute_ProjectProperty_DescriptorInput_NameOutput_PropertyGeneratedCorrectly()
            throws LazyInitializationException {
        task.setProject( MockModel.Projects.buildOrderTest.get() );
        task.setProperty( "property" );
        task.setInputMode( ResolutionModes.descriptors );
        task.setOutputMode( ResolutionModes.names );
        task.execute();
        final String property = project.getProperty( "property" );
        assertNotNull( "Property was not generated.", property );
        assertEquals( "Build order incorrectly resolved.", nameBuildOrder, property );
    }

    @Test
    public void execute_ProjectProperty_DescriptorInput_DescriptorOutput_PropertyGeneratedCorrectly()
            throws LazyInitializationException {
        task.setProject( MockModel.Projects.buildOrderTest.get() );
        task.setProperty( "property" );
        task.setInputMode( ResolutionModes.descriptors );
        task.setOutputMode( ResolutionModes.descriptors );
        task.execute();
        final String property = project.getProperty( "property" );
        assertNotNull( "Property was not generated.", property );
        assertEquals( "Build order incorrectly resolved.", descriptorBuildOrder, property );
    }

    @Test
    public void execute_ProjectProperty_DescriptorInput_OutputUnspecified_PropertyGeneratedCorrectly()
            throws LazyInitializationException {
        task.setProject( MockModel.Projects.buildOrderTest.get() );
        task.setProperty( "property" );
        task.setInputMode( ResolutionModes.descriptors );
        task.execute();
        final String property = project.getProperty( "property" );
        assertNotNull( "Property was not generated.", property );
        assertEquals( "Build order incorrectly resolved.", descriptorBuildOrder, property );
    }

    public void execute_ModuleNamesSpecified_PropertyGeneratedCorrectly() throws LazyInitializationException {
        task.setProject( MockModel.Projects.buildOrderTest.get() );
        task.setProperty( "property" );
        task.setInputMode( ResolutionModes.names );
        task.setModules( "build-order-test-a,build-order-test-b" );
        task.execute();
        final String property = project.getProperty( "property" );
        assertNotNull( "Property was not generated.", property );
        assertEquals( "Build order incorrectly resolved.",
                "build-order-test-b,build-order-test-a",
                property );
    }
}
