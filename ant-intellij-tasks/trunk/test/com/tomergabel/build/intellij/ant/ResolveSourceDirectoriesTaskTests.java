package com.tomergabel.build.intellij.ant;

import static com.tomergabel.build.intellij.model.MockModel.Modules.outputModuleRelative;
import static com.tomergabel.build.intellij.model.MockModel.Modules.outputProjectRelative;
import static com.tomergabel.build.intellij.model.MockModel.Projects;
import com.tomergabel.util.LazyInitializationException;
import static com.tomergabel.util.TestUtils.assertSetEquality;
import static junit.framework.Assert.assertNull;
import org.apache.tools.ant.BuildException;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

public class ResolveSourceDirectoriesTaskTests {


    @Before
    public void testSetup() {

    }

    @Test
    public void resolveSourceDirectories_OutputProjectRelativeButProjectNotSpecified_BuildExceptionThrown()
            throws URISyntaxException, LazyInitializationException {
        final ResolveSourceDirectoriesTask task = new ResolveSourceDirectoriesTask();
        task.setModule( outputProjectRelative.get() );
        try {
            task.resolveSourceDirectories();
            fail( "Resolution did not fail even though project file was not specified and " +
                    "source directory is project-relative." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void resolveSourceDirectories_OutputProjectRelativeButProjectNotSpecified_NoFailOnError_ReturnsNull()
            throws URISyntaxException, LazyInitializationException {
        final ResolveSourceDirectoriesTask task = new ResolveSourceDirectoriesTask();
        task.setModule( outputProjectRelative.get() );
        task.setFailonerror( false );
        assertNull( task.resolveSourceDirectories() );
    }

    @Test
    public void resolveSourceDirectories_OutputProjectRelativeAndProjectSpecified_Both_DirectoriesResolvedCorrectly()
            throws Exception {
        final ResolveSourceDirectoriesTask task = new ResolveSourceDirectoriesTask();
        task.setProject( Projects.allModules.get() );
        task.setModule( outputProjectRelative.get() );
        assertSetEquality( "Source directories not resolved correctly.", new String[] {
                new File( task.project().getProjectRoot().resolve( "src" ) ).getAbsolutePath(),
                new File( task.project().getProjectRoot().resolve( "test" ) ).getAbsolutePath(),
        }, task.resolveSourceDirectories() );
    }

    @Test
    public void resolveSourceDirectories_OutputNotProjectRelativeAndProjectNotSpecified_DirectoriesResolvedCorrectly()
            throws URISyntaxException, LazyInitializationException {
        final ResolveSourceDirectoriesTask task = new ResolveSourceDirectoriesTask();
        task.setModule( outputModuleRelative.get() );
        assertSetEquality( "Source directories not resolved correctly.", new String[] {
                new File( task.module().getModuleRoot().resolve( "src" ) ).getAbsolutePath(),
                new File( task.module().getModuleRoot().resolve( "test" ) ).getAbsolutePath(),
        }, task.resolveSourceDirectories() );
    }

    @Test
    public void resolveSourceDirectories_ProjectWithTestFilesAndSourceFilterSpecified_OnlySourceDirectoriesReturned()
            throws URISyntaxException, LazyInitializationException {
        final ResolveSourceDirectoriesTask task = new ResolveSourceDirectoriesTask();
        task.setModule( outputModuleRelative.get() );
        assertSetEquality( "Source directories not resolved correctly.", new String[] {
                new File( task.module().getModuleRoot().resolve( "src" ) ).getAbsolutePath(),
        }, task.resolveSourceDirectories( Filter.source ) );
    }

    @Test
    public void resolveSourceDirectories_ModuleWithTestFilesAndTestFilterSpecified_OnlyTestDirectoriesReturned()
            throws URISyntaxException, LazyInitializationException {
        final ResolveSourceDirectoriesTask task = new ResolveSourceDirectoriesTask();
        task.setModule( outputModuleRelative.get() );
        assertSetEquality( "Source directories not resolved correctly.", new String[] {
                new File( task.module().getModuleRoot().resolve( "test" ) ).getAbsolutePath(),
        }, task.resolveSourceDirectories( Filter.test ) );
    }

    @Test
    public void resolveSourceDirectories_ModuleWithTestFilesAndBothFilterSpecified_SourceAndTestDirectoriesReturned()
            throws URISyntaxException, LazyInitializationException {
        final ResolveSourceDirectoriesTask task = new ResolveSourceDirectoriesTask();
        task.setModule( outputModuleRelative.get() );
        assertSetEquality( "Source directories not resolved correctly.", new String[] {
                new File( task.module().getModuleRoot().resolve( "src" ) ).getAbsolutePath(),
                new File( task.module().getModuleRoot().resolve( "test" ) ).getAbsolutePath(),
        }, task.resolveSourceDirectories( Filter.both ) );
    }

    // TODO finish tests!

    @Test
    public void execute_NoModuleSpecified_ThrowsBuildException() {
    }

    @Test
    public void execute_NoModuleSpecifiedAndNoFailOnError_NothingHappens() {
    }

    @Test
    public void execute_OutputNotProjectRelativeAndProjectNotSpecified_PropertyGeneratedCorrectly() {
    }

    @Test
    public void execute_OutputNotProjectRelativeAndProjectNotSpecified_PathGeneratedCorrectly() {
    }

    @Test
    public void execute_OutputNotProjectRelativeAndProjectNotSpecified_PathAndPathGeneratedCorrectly() {
    }

    @Test
    public void execute_OutputProjectRelativeAndProjectSpecified_ThrowsBuildException() {
    }

    @Test
    public void execute_OutputProjectRelativeAndProjectSpecifiedAndNoFailOnError_NothingHappens() {
    }

    @Test
    public void execute_OutputProjectRelativeAndProjectSpecified_PropertyGeneratedCorrectly() {
    }

    @Test
    public void execute_OutputProjectRelativeAndProjectSpecified_PathGeneratedCorrectly() {
    }

    @Test
    public void execute_OutputProjectRelativeAndProjectSpecified_PathAndPathGeneratedCorrectly() {
    }

}
