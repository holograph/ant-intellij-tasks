package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.MockModel;
import com.tomergabel.build.intellij.model.Module;
import com.tomergabel.build.intellij.model.ModuleResolver;
import com.tomergabel.build.intellij.model.ResolutionException;
import com.tomergabel.util.LazyInitializationException;
import com.tomergabel.util.UriUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.FileSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
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

    @Before
    public void setup() {
        final Project project = new Project();
        this.task = new PackageModuleJarTask() {
            @Override
            protected Jar instantiateJarTask() {
                return new TestableJar();
            }
        };
        this.task.setProject( project );
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
    public void createJarTask_SelfContainedModuleSpecified_JarCreatedCorrectly()
            throws LazyInitializationException, ResolutionException {
        final Module module = MockModel.Modules.jarOutputSelfContained.get();
        this.task.setModule( module );
        final TestableJar jar = (TestableJar) this.task.createJarTask();

        assertEquals( "JAR output URL incorrectly generated.", UriUtils.getFile(
                new ModuleResolver( module ).resolveUriString( module.getJarSettings().getJarUrl() ) ),
                jar.getDestFile() );
        // TODO test JAr resource generation
    }

    @Test
    public void createJarTask_DependentModuleSpecified_JarTaskCreatedSuccessfully() {
        // TODO
    }
}
