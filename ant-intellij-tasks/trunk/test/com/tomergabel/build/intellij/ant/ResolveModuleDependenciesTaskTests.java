package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.Module;
import static com.tomergabel.util.TestUtils.assertSetEquality;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.tools.ant.BuildException;

import java.net.URI;

public class ResolveModuleDependenciesTaskTests {
    private static URI module;
    private static URI project;
    private static URI dependee;

    @BeforeClass
    public static void testSetup() throws Exception {
        module = ResolveModuleDependenciesTask.class.getResource( "../model/parsing-test.iml" ).toURI();
        project = ResolveModuleDependenciesTask.class.getResource( "../model/parsing-test.ipr" ).toURI();
        dependee = ResolveModuleDependenciesTask.class.getResource( "../model/dependee.iml" ).toURI();
    }

    @Test
    public void testDepdencyResolution_ProjectNotSpecifiedWithNoModuleDependencies_EmptyCollectionReturned()
            throws Exception {
        final ResolveModuleDependenciesTask task = new ResolveModuleDependenciesTask();
        task.setModuleDescriptor( dependee );
        assertEquals( "Depdency list incorrectly parsed.", 0, task.resolveModules().size() );
    }

    @Test
    public void testDepdencyResolution_ProjectNotSpecifiedWithModuleDependencies_BuildExceptionIsThrown()
            throws Exception {
        final ResolveModuleDependenciesTask task = new ResolveModuleDependenciesTask();
        task.setModuleDescriptor( module );
        try {
            task.resolveModules();
            fail( "Resolution did not fail even though project file was not specified and dependencies are present." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testDepdencyResolution_ProjectSpecifiedWithModuleDependencies_DependenciesResolvedCorrectly()
            throws Exception {
        final ResolveModuleDependenciesTask task = new ResolveModuleDependenciesTask();
        task.setModuleDescriptor( module );
        task.setProjectDescriptor( project );
        assertSetEquality( "Model dependency resolution failed.", new Module[] { Module.parse( dependee ) },
                task.resolveModules() );
    }
}
