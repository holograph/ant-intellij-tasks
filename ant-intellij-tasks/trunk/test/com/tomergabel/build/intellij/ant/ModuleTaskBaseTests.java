package com.tomergabel.build.intellij.ant;

import org.apache.tools.ant.BuildException;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;
import java.net.URI;

@RunWith( Suite.class )
@Suite.SuiteClasses( value = { ModuleTaskBaseTests.FailOnError.class, ModuleTaskBaseTests.NoFailOnError.class } )
public class ModuleTaskBaseTests {

    public abstract static class BaseTest {
        ModuleTaskBase task;

        @Before
        public void testSetup() {
            this.task = new ModuleTaskBase() {
                @Override
                protected void executeTask() throws BuildException {}
            };
        }

        private void assertFailure( final String message ) {
            if ( message == null )
                throw new IllegalArgumentException( "Invalid test case: assertion failure message not specified" );

            try {
                this.task.module();
                fail( message + ", BuildException expected" );
            } catch ( BuildException e ) {
                // Expected, all is well
            }
        }

        @Test
        public void module_NoModuleSpecified_ErrorPropagated() {
            assertFailure( "Module not specified" );
        }

        // TODO srcfile tests

        @Test
        public void module_InvalidModuleDescriptorSpecified_ErrorPropagated() throws Exception {
            // Try opaque URI
            this.task.setModuleDescriptor( new URI( "about:config" ) );
            assertFailure( "Invalid module descriptor specified" );
        }

        @Test
        public void module_ValidModuleDesciptorSpecifiedButFileDoesNotExist_ErrorPropagated() throws Exception {
            final File f = File.createTempFile( "moduletest", null );
            f.delete();
            this.task.setModuleDescriptor( f.toURI() );
            assertFailure( "Module descriptor points to a nonexistant file" );
        }

        @Test
        public void module_ValidModuleDesciptorButInvalidModuleSpecified_ErrorPropagated() throws Exception {
            final File f = File.createTempFile( "moduletest", null );
            try {
                // At this point the file exists but is empty
                this.task.setModuleDescriptor( f.toURI() );
                assertFailure( "Module descriptor points to an invalid file" );
            } finally {
                f.delete();
            }
        }

        @Test
        public void project_ValidProjectDescriptorSpecifiedButFileDoesNotExist_ErrorPropagated() throws Exception {
            final File f = File.createTempFile( "projecttest", null );
            f.delete();
            this.task.setProjectDescriptor( f.toURI() );
            assertFailure( "Project descriptor points to an nonexistant file" );
        }

        @Test
        public void project_InvalidProjectDescriptorSpecified_ErrorPropagated() throws Exception {
            // Try opaque URI
            this.task.setProjectDescriptor( new URI( "about:config" ) );
            assertFailure( "Invalid project descriptor specified" );
        }

        @Test
        public void project_ValidProjectDescriptorButInvalidProjectSpecified_ErrorPropagated() throws Exception {
            final File f = File.createTempFile( "projecttest", null );
            try {
                // At this point the file exists but is empty
                this.task.setProjectDescriptor( f.toURI() );
                assertFailure( "Project descriptor points to an invalid file" );
            } finally {
                f.delete();
            }
        }

    }

    public static class FailOnError extends BaseTest {
    }

    public static class NoFailOnError extends BaseTest {
        @Override
        public void testSetup() {
            super.testSetup();
            this.task.setFailonerror( false );
        }
    }

    @Test
    public void module_ValidModuleDescriptorSpecified_ReturnsModule() {
        // TODO
    }

    @Test
    public void project_ValidProjectDescriptorSpecified_ReturnsProject() {
        // TODO
    }

    // TODO suppress output
    // TODO ordering tests
    // TODO resolver() tests
}


