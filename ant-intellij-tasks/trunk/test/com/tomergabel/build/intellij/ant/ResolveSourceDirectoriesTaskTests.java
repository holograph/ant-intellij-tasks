package com.tomergabel.build.intellij.ant;

import static com.tomergabel.util.TestUtils.assertSetEquality;
import org.apache.tools.ant.BuildException;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

public class ResolveSourceDirectoriesTaskTests {
    @Test
    public void resolveSourceDirectories_OutputProjectRelativeButProjectNotSpecified_BuildExceptionThrown()
            throws URISyntaxException {
        final ResolveSourceDirectoriesTask task = new ResolveSourceDirectoriesTask();
        task.setModuleDescriptor( this.getClass().getResource( "output-project-relative.iml" ).toURI() );
        try {
            task.resolveSourceDirectories();
            fail( "Resolution did not fail even though project file was not specified and " +
                    "source directory is project-relative." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void resolveSourceDirectories_OutputProjectRelativeAndProjectSpecified_DirectoriesResolvedCorrectly()
            throws URISyntaxException {
        final ResolveSourceDirectoriesTask task = new ResolveSourceDirectoriesTask();
        task.setProjectDescriptor( this.getClass().getResource( "../model/parsing-test.ipr" ).toURI() );
        task.setModuleDescriptor( this.getClass().getResource( "output-project-relative.iml" ).toURI() );
        assertSetEquality( "Source directories not resolved correctly.", new String[] {
                new File( task.project().getProjectRoot().resolve( "src" ) ).getAbsolutePath(),
                new File( task.project().getProjectRoot().resolve( "test" ) ).getAbsolutePath(),
        }, task.resolveSourceDirectories() );
    }

    @Test
    public void resolveSourceDirectories_OutputNotProjectRelativeAndProjectNotSpecified_DirectoriesResolvedCorrectly()
            throws URISyntaxException {
        final ResolveSourceDirectoriesTask task = new ResolveSourceDirectoriesTask();
        task.setModuleDescriptor( this.getClass().getResource( "output-module-relative.iml" ).toURI() );
        assertSetEquality( "Source directories not resolved correctly.", new String[] {
                new File( task.module().getModuleRoot().resolve( "src" ) ).getAbsolutePath(),
                new File( task.module().getModuleRoot().resolve( "test" ) ).getAbsolutePath(),
        }, task.resolveSourceDirectories() );
    }

    @Test
    public void resolveSourceDirectories_ProjectWithTestFilesButTestsNotIncluded_OnlySourceDirectoriesReturned()
            throws URISyntaxException {
        final ResolveSourceDirectoriesTask task = new ResolveSourceDirectoriesTask();
        task.setIncludeTestDirectories( false );
        task.setModuleDescriptor( this.getClass().getResource( "output-module-relative.iml" ).toURI() );
        assertSetEquality( "Source directories not resolved correctly.", new String[] {
                new File( task.module().getModuleRoot().resolve( "src" ) ).getAbsolutePath(),
        }, task.resolveSourceDirectories() );
    }
}
