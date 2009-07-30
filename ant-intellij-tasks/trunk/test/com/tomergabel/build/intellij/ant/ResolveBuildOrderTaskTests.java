package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.MockModel;
import com.tomergabel.util.LazyInitializationException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.assertNull;

public class ResolveBuildOrderTaskTests {
    ResolveBuildOrderTask task;
    Project project;

    @Before
    public void testSetup() {
        this.task = new ResolveBuildOrderTask();
        this.project = new Project();
        task.setProject( this.project );
    }

    @Test
    public void test_NoProjectSpecified_BuildExceptionIsThrown() {
        try {
            task.setProperty( "property" );
            task.executeTask();
            fail( "Project not specified but no exception was thrown." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void test_NoProjectSpecified_NoFailOnError_NothingHappens() {
        task.setProperty( "property" );
        task.setFailonerror( false );
        task.executeTask();
        assertNull( "Property generated, no such behavior expected.", project.getProperty( "property" ) );
    }

    @Test
    public void test_NoPropertySpecified_BuildExceptionIsThrown() throws LazyInitializationException {
        try {
            task.setProject( MockModel.Projects.allModules.get() );
            task.executeTask();
            fail( "Property not specified but no exception was thrown." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void test_NoPropertySpecified_NoFailOnError_NothingHappens() throws LazyInitializationException {
        task.setProject( MockModel.Projects.allModules.get() );
        task.setFailonerror( false );
        task.executeTask();
        assertNull( "Property generated, no such behavior expected.", project.getProperty( "property" ) );
    }

    @Test
    public void test_ProjectPropertyAndNamesModeSpecified_PropertyGeneratedCorrectly() {
    }

    @Test
    public void test_ProjectPropertyAndDescriptorsModeSpecified_PropertyGeneratedCorrectly() {
    }
}
