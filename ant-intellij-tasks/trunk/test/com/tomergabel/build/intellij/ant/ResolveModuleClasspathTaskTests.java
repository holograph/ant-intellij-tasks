package com.tomergabel.build.intellij.ant;

import static com.tomergabel.build.intellij.model.MockModel.Modules.dependantLibrary;
import static com.tomergabel.build.intellij.model.MockModel.Modules.dependantModule;
import static com.tomergabel.build.intellij.model.MockModel.Projects.allModules;
import com.tomergabel.build.intellij.model.ModuleResolver;
import static com.tomergabel.util.TestUtils.assertSetEquality;
import static junit.framework.Assert.assertNotNull;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

@SuppressWarnings( { "ConstantConditions" } )
public class ResolveModuleClasspathTaskTests extends AntTestBase {
    public ResolveModuleClasspathTaskTests() throws URISyntaxException, IOException, ClassNotFoundException {
        super();
    }

    private ResolveModuleClasspathTask task;

    @Before
    public void setup() {
        this.task = new ResolveModuleClasspathTask();
        task.setProject( this.project );
    }

    @Test
    public void testExecute_ModuleNotSpecified_ThrowsBuildException() throws Exception {
        try {
            this.task.execute();
            fail( "Module not specified, expected BuildException" );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testExecute_DependantModuleWithNoProjectSpecified_ThrowsBuildException() throws Exception {
        // Build task

        task.setModule( dependantModule.get() );
        try {
            this.task.execute();
            fail( "Module not specified, expected BuildException" );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testExecute_ProjectLibraryDependencyWithNoProjectSpecified_ThrowsBuildException() throws Exception {
        task.setModule( dependantLibrary.get() );
        try {
            this.task.execute();
            fail( "Module not specified, expected BuildException" );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testExecute_DependantModuleWithProjectSpecifiedButNoPathId_ThrowsBuildException() throws Exception {
        task.setModule( dependantModule.get() );
        task.setProject( allModules.get() );
        try {
            this.task.execute();
            fail( "Module not specified, expected BuildException" );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testExecute_ProjectLibraryDependencyWithProjectSpecifiedButNoPathId_ThrowsBuildException()
            throws Exception {
        task.setModule( dependantLibrary.get() );
        task.setProject( allModules.get() );
        try {
            this.task.execute();
            fail( "Module not specified, expected BuildException" );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    // TODO add filtering tests

    @Test
    public void testExecute_DependantModuleWithProjectAndPathIdSpecified_CorrectClasspathResolved() throws Exception {
        task.setModule( dependantModule.get() );
        task.setProject( allModules.get() );
        task.setPathId( "testpath" );
        task.execute();

        // Assert on generatd classpath
        final Object object = project.getReference( "testpath" );
        assertNotNull( "Classpath was not generated.", object );
        assertTrue( "Generated object is not a Path.", object instanceof Path );
        assertSetEquality( "Classpath generated incorrectly.",
                new ModuleResolver( allModules.get(), dependantModule.get() ).resolveModuleClasspath( true, false ),
                ( (Path) object ).list() );
    }

    @Test
    public void testExecute_ProjectLibraryDependencyWithProjectAndPathIdSpecified_CorrectClasspathResolved()
            throws Exception {
        task.setModule( dependantLibrary.get() );
        task.setProject( allModules.get() );
        task.setPathId( "testpath" );
        this.task.execute();

        // Assert on generatd classpath
        final Object object = project.getReference( "testpath" );
        assertNotNull( "Classpath was not generated.", object );
        assertTrue( "Generated object is not a Path.", object instanceof Path );
        assertSetEquality( "Classpath generated incorrectly.",
                new ModuleResolver( allModules.get(), dependantLibrary.get() ).resolveModuleClasspath( true, false ),
                ( (Path) object ).list() );
    }
}
