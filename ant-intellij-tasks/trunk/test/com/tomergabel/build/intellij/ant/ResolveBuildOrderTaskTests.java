package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.MockModel;
import com.tomergabel.util.CollectionUtils;
import com.tomergabel.util.LazyInitializationException;
import com.tomergabel.util.UriUtils;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

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
            task.execute();
            fail( "Project not specified but no exception was thrown." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void test_NoProjectSpecified_NoFailOnError_NothingHappens() {
        task.setProperty( "property" );
        task.setFailonerror( false );
        task.execute();
        assertNull( "Property generated, no such behavior expected.", project.getProperty( "property" ) );
    }

    @Test
    public void test_NoPropertySpecified_BuildExceptionIsThrown() throws LazyInitializationException {
        try {
            task.setProject( MockModel.Projects.buildOrderTest.get() );
            task.execute();
            fail( "Property not specified but no exception was thrown." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void test_NoPropertySpecified_NoFailOnError_NothingHappens() throws LazyInitializationException {
        task.setProject( MockModel.Projects.buildOrderTest.get() );
        task.setFailonerror( false );
        task.execute();
        assertNull( "Property generated, no such behavior expected.", project.getProperty( "property" ) );
    }

    @Test
    public void test_ProjectPropertyAndNamesModeSpecified_PropertyGeneratedCorrectly()
            throws LazyInitializationException {
        task.setProject( MockModel.Projects.buildOrderTest.get() );
        task.setProperty( "property" );
        task.setMode( ResolutionModes.names );
        task.setFailonerror( false );
        task.execute();
        final String property = project.getProperty( "property" );
        assertNotNull( "Property was not generated.", property );
        assertEquals( "Build order incorrectly resolved.",
                "build-order-test-d,build-order-test-c,build-order-test-b,build-order-test-a",
                property );
    }

    @Test
    public void test_ProjectPropertyAndDescriptorsModeSpecified_PropertyGeneratedCorrectly()
            throws LazyInitializationException {
        task.setProject( MockModel.Projects.buildOrderTest.get() );
        task.setProperty( "property" );
        task.setMode( ResolutionModes.descriptors );
        task.setFailonerror( false );
        task.execute();
        final String property = project.getProperty( "property" );
        assertNotNull( "Property was not generated.", property );
        assertEquals( "Build order incorrectly resolved.",
                CollectionUtils.join( ResolveBuildOrderTask.LIST_SEPARATOR,
                        UriUtils.getPath( MockModel.Modules.buildOrderTestD.get().getModuleDescriptor() ),
                        UriUtils.getPath( MockModel.Modules.buildOrderTestC.get().getModuleDescriptor() ),
                        UriUtils.getPath( MockModel.Modules.buildOrderTestB.get().getModuleDescriptor() ),
                        UriUtils.getPath( MockModel.Modules.buildOrderTestA.get().getModuleDescriptor() )
                ), property );
    }
}
