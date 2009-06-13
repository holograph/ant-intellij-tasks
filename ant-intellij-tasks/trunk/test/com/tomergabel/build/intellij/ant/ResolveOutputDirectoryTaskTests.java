package com.tomergabel.build.intellij.ant;

import org.apache.tools.ant.BuildException;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

public class ResolveOutputDirectoryTaskTests {
    @Test
    public void resolveOutputDirectory_ProjectNotSpecified_OutputWithProjectRelativePath_BuildExceptionThrown()
            throws URISyntaxException {
        final ResolveOutputDirectoryTask task = new ResolveOutputDirectoryTask();
        task.setModuleDescriptor( this.getClass().getResource( "output-project-relative.iml" ).toURI() );
        try {
            task.resolveOutputDirectory();
            fail( "Resolution did not fail even though project file was not specified and " +
                    "output directory is project-relative." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void resolveOutputDirectory_ProjectNotSpecified_OutputWithoutProjectRelativePath_PathResolvedCorrectly()
            throws URISyntaxException {
        final ResolveOutputDirectoryTask task = new ResolveOutputDirectoryTask();
        task.setModuleDescriptor( this.getClass().getResource( "output-module-relative.iml" ).toURI() );
        assertEquals( "Output directory resolved incorrectly.",
                new File( task.module().getModuleRoot().resolve( "bin" ) ).getAbsolutePath(),
                task.resolveOutputDirectory() );
    }

    @Test
    public void resolveOutputDirectory_ProjectSpecified_OutputWithProjectRelativePath_PathResolvedCorrectly()
            throws URISyntaxException {
        final ResolveOutputDirectoryTask task = new ResolveOutputDirectoryTask();
        task.setModuleDescriptor( this.getClass().getResource( "output-project-relative.iml" ).toURI() );
        task.setProjectDescriptor( this.getClass().getResource( "../model/parsing-test.ipr" ).toURI() );
        assertEquals( "Output directory resolved incorrectly.",
                new File( task.project().getProjectRoot().resolve( "bin" ) ).getAbsolutePath(),
                task.resolveOutputDirectory() );
    }
}
