package com.tomergabel.build.intellij.ant;

import static com.tomergabel.build.intellij.model.MockModel.Modules.dependantLibrary;
import static com.tomergabel.build.intellij.model.MockModel.Modules.dependantModule;
import static com.tomergabel.build.intellij.model.MockModel.Projects.allModules;
import static com.tomergabel.build.intellij.model.MockModel.junitLibraryPath;
import com.tomergabel.build.intellij.model.ModuleResolver;
import static com.tomergabel.util.TestUtils.assertSetEquality;
import static junit.framework.Assert.assertNotNull;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.types.Path;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.util.Collections;

@SuppressWarnings( { "ConstantConditions" } )
public class ResolveModuleClasspathTaskTests extends AntTestBase {

    @Test
    public void testExecute_ModuleNotSpecified_ThrowsBuildException() throws Exception {
        final ResolveModuleClasspathTask task = new ResolveModuleClasspathTask();
        try {
            execute( task );
            fail( "Module not specified, expected BuildException" );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testExecute_DependantModuleWithNoProjectSpecified_ThrowsBuildException() throws Exception {
        // Build task
        final ResolveModuleClasspathTask task = new ResolveModuleClasspathTask();
        task.setModule( dependantModule.get() );
        try {
            execute( task );
            fail( "Module not specified, expected BuildException" );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testExecute_ProjectLibraryDependencyWithNoProjectSpecified_ThrowsBuildException() throws Exception {
        // Build task
        final ResolveModuleClasspathTask task = new ResolveModuleClasspathTask();
        task.setModule( dependantLibrary.get() );
        try {
            execute( task );
            fail( "Module not specified, expected BuildException" );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testExecute_DependantModuleWithProjectSpecifiedButNoPathId_ThrowsBuildException() throws Exception {
        final ResolveModuleClasspathTask task = new ResolveModuleClasspathTask();
        task.setModule( dependantModule.get() );
        task.setProject( allModules.get() );
        try {
            execute( task );
            fail( "Module not specified, expected BuildException" );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testExecute_ProjectLibraryDependencyWithProjectSpecifiedButNoPathId_ThrowsBuildException() throws Exception {
        final ResolveModuleClasspathTask task = new ResolveModuleClasspathTask();
        task.setModule( dependantLibrary.get() );
        task.setProject( allModules.get() );
        try {
            execute( task );
            fail( "Module not specified, expected BuildException" );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testExecute_DependantModuleWithProjectAndPathIdSpecified_CorrectClasspathResolved() throws Exception {
        final ResolveModuleClasspathTask task = new ResolveModuleClasspathTask();
        task.setModule( dependantModule.get() );
        task.setProject( allModules.get() );
        task.setPathId( "testpath" );
        final Project project = execute( task );

        // Assert on generatd classpath
        final Object object = project.getReference( "testpath" );
        assertNotNull( "Classpath was not generated.", object );
        assertTrue( "Generated object is not a Path.", object instanceof Path );
        assertSetEquality( "Classpath generated incorrectly.",
                new ModuleResolver( allModules.get(), dependantModule.get() ).resolveModuleClasspath(),
                ( (Path) object ).list() );
    }

    @Test
    public void testExecute_ProjectLibraryDependencyWithProjectAndPathIdSpecified_CorrectClasspathResolved() throws Exception {
        final ResolveModuleClasspathTask task = new ResolveModuleClasspathTask();
        task.setModule( dependantLibrary.get() );
        task.setProject( allModules.get() );
        task.setPathId( "testpath" );
        final Project project = execute( task );

        // Assert on generatd classpath
        final Object object = project.getReference( "testpath" );
        assertNotNull( "Classpath was not generated.", object );
        assertTrue( "Generated object is not a Path.", object instanceof Path );
        assertSetEquality( "Classpath generated incorrectly.",
                Collections.singleton( junitLibraryPath.get() ),
                ( (Path) object ).list() );
    }

    private Project execute( final ResolveModuleClasspathTask task ) {
        // Build project around task
        final Project project = new Project();
        project.setDefault( "build" );
        final Target buildTarget = new Target();
        buildTarget.setProject( project );
        buildTarget.setName( "build" );
        buildTarget.addTask( task );
        task.setProject( project );
        project.addTarget( buildTarget );

        // Execute project
        project.executeTarget( project.getDefaultTarget() );
        return project;
    }
}
