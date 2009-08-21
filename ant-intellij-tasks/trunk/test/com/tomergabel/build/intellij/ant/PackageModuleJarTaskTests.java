package com.tomergabel.build.intellij.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.FileSet;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;

@Ignore( "Pending completion" )
public class PackageModuleJarTaskTests {
    private PackageModuleJarTask task;

    static class TestableJar extends Jar {
        public FileSet list() {
            return this.getImplicitFileSet();
        }
    }

    @Test
    public void execute_ModuleNotSpecified_BuildExceptionIsThrown() {
        try {
            task.execute();
            fail( "Module was not specified, BuildException expected." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void createJarTask_SelfContainedModuleSpecified_JarCreatedCorrectly() {
        // TODO
    }

    @Test
    public void createJarTask_DependentModuleSpecified_JarTaskCreatedSuccessfully() {
        // TODO
    }
}
