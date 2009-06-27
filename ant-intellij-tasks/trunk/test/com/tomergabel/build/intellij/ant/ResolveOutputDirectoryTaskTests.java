package com.tomergabel.build.intellij.ant;

import org.apache.tools.ant.BuildException;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URI;

public class ResolveOutputDirectoryTaskTests {
    static class ProjectOutputs {
        static final URI specified;
        static final URI unspecified;

        static {
            try {
                specified = ResolveOutputDirectoryTaskTests.class.getResource( "output-specified.ipr" ).toURI();
                unspecified = ResolveOutputDirectoryTaskTests.class.getResource( "output-specified.ipr" ).toURI();
            } catch ( URISyntaxException e ) {
                throw new RuntimeException( "Can't load test resource", e );
            }
        }
    }

    static class ModuleOutputs {
        static final URI unspecified;
        static final URI projectRelative;
        static final URI moduleRelative;

        static {
            try {
                unspecified = ResolveOutputDirectoryTaskTests.class.getResource( "output-unspecified.iml" ).toURI();
                projectRelative = ResolveOutputDirectoryTaskTests.class.getResource( "output-project-relative.iml" )
                        .toURI();
                moduleRelative = ResolveOutputDirectoryTaskTests.class.getResource( "output-module-relative.iml" )
                        .toURI();
            } catch ( URISyntaxException e ) {
                throw new RuntimeException( "Can't load test resource", e );
            }
        }
    }

    ResolveOutputDirectoryTask task;

    @Before
    public void testSetup() {
        this.task = new ResolveOutputDirectoryTask();
    }

    @Test
    public void resolveOutputDirectory_ProjectNotSpecified_OutputWithProjectRelativePath_BuildExceptionThrown()
            throws URISyntaxException {
        this.task.setModuleDescriptor( ModuleOutputs.projectRelative );
        try {
            this.task.resolveOutputDirectory();
            fail( "Resolution did not fail even though project file was not specified and " +
                    "output directory is project-relative." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void resolveOutputDirectory_ProjectNotSpecified_OutputWithoutProjectRelativePath_PathResolvedCorrectly()
            throws URISyntaxException {
        this.task.setModuleDescriptor( ModuleOutputs.moduleRelative );
        assertEquals( "Output directory resolved incorrectly.",
                new File( this.task.module().getModuleRoot().resolve( "bin" ) ).getAbsolutePath(),
                this.task.resolveOutputDirectory() );
    }

    @Test
    public void resolveOutputDirectory_ProjectSpecified_OutputWithProjectRelativePath_PathResolvedCorrectly()
            throws URISyntaxException {
        this.task.setModuleDescriptor( ModuleOutputs.projectRelative );
        this.task.setProjectDescriptor( ProjectOutputs.specified );
        assertEquals( "Output directory resolved incorrectly.",
                new File( this.task.project().getProjectRoot().resolve( "bin" ) ).getAbsolutePath(),
                this.task.resolveOutputDirectory() );
    }

    @Test
    public void resolveOutputDirectory_ProjectNotSpecified_UnspecifiedModuleOutput_BuildExceptionThrown()
            throws URISyntaxException {
        this.task.setModuleDescriptor( ModuleOutputs.unspecified );
        try {
            this.task.resolveOutputDirectory();
            fail( "Resolution did not fail even though project file was not specified and " +
                    "module does not specify output directory." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void resolveOutputDirectory_ProjectWithOutputSpecified_UnspecifiedModuleOutput_FallsBackToProjectOutput()
            throws URISyntaxException {
        this.task.setModuleDescriptor( ModuleOutputs.unspecified );
        this.task.setProjectDescriptor( ProjectOutputs.specified );
        assertEquals( "Output directory resolved incorrectly.",
                new File( this.task.project().getOutputUrl() ).getAbsolutePath(),
                this.task.resolveOutputDirectory() );
    }

    @Test
    public void resolveOutputDirectory_ProjectWithNoOutputSpecified_UnspecifiedModuleOutput_BuildExceptionIsThrown()
            throws URISyntaxException {
        this.task.setModuleDescriptor( ModuleOutputs.unspecified );
        this.task.setProjectDescriptor( ProjectOutputs.unspecified );
        try {
            this.task.resolveOutputDirectory();
            fail( "Resolution did not fail even though project does not specify output URL and " +
                    "module does not specify output directory." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    // TODO failOnError tests
}
