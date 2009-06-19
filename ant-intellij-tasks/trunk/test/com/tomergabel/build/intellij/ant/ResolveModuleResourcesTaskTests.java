package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.MockModel;
import static com.tomergabel.util.TestUtils.assertSetEquality;
import static junit.framework.Assert.assertNotNull;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.File;

public class ResolveModuleResourcesTaskTests {
    @Test
    public void execute_ModuleNotSpecified_ThrowsBuildException() {
        final ResolveModuleResourcesTask task = new ResolveModuleResourcesTask();
        try {
            task.execute();
            fail( "Module not specified, BuildException expected." );
        } catch ( BuildException e ) {
            // Expected, all is well.
        }
    }

    @Test
    public void execute_ModuleNotSpecified_NoFailOnError_NothingHappens() {
        final ResolveModuleResourcesTask task = new ResolveModuleResourcesTask();
        task.setFailonerror( false );
        task.execute();
    }

    @Test
    public void execute_PathIdNotSpecified_ThrowsBuildException() throws Exception {
        final ResolveModuleResourcesTask task = new ResolveModuleResourcesTask();
        task.setModule( MockModel.selfContained.get() );
        try {
            task.execute();
            fail( "Module not specified, BuildException expected." );
        } catch ( BuildException e ) {
            // Expected, all is well.
        }
    }

    @Test
    public void execute_PathIdNotSpecified_NoFailOnError_NothingHappens() throws Exception {
        final ResolveModuleResourcesTask task = new ResolveModuleResourcesTask();
        task.setFailonerror( false );
        task.setModule( MockModel.selfContained.get() );
        task.execute();
    }

    @Test
    public void execute_NoProjectSpecified_EmptyPathGenerated() throws Exception {
        final Project project = new Project();
        final ResolveModuleResourcesTask task = new ResolveModuleResourcesTask();
        task.setProject( project );
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
        final Project project = new Project();
        final ResolveModuleResourcesTask task = new ResolveModuleResourcesTask();
        task.setProject( project );
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
                new File( this.getClass().getResource( "resource-extension.gif" ).toURI() ).getAbsolutePath(),
                new File( this.getClass().getResource( "resource-pattern.jpg" ).toURI() ).getAbsolutePath()
        }, path.list() );
    }
}
