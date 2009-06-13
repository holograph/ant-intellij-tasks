package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.MockModel;
import static com.tomergabel.util.TestUtils.assertSetEquality;
import org.apache.tools.ant.BuildException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.util.Collections;

public class ResolveModuleDependenciesTaskTests {
    @Test
    public void testDepdencyResolution_ProjectNotSpecifiedWithNoModuleDependencies_EmptyCollectionReturned()
            throws Exception {
        final ResolveModuleDependenciesTask task = new ResolveModuleDependenciesTask();
        task.setModule( MockModel.dependee.get() );
        assertEquals( "Depdency list incorrectly parsed.", 0, task.resolveModules().size() );
    }

    @Test
    public void testDepdencyResolution_ProjectNotSpecifiedWithModuleDependencies_BuildExceptionIsThrown()
            throws Exception {
        final ResolveModuleDependenciesTask task = new ResolveModuleDependenciesTask();
        task.setModule( MockModel.dependantModule.get() );
        try {
            task.resolveModules();
            fail( "Resolution did not fail even though project file was not specified and " +
                    "module dependencies are present." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testDepdencyResolution_ProjectSpecifiedWithModuleDependencies_DependenciesResolvedCorrectly()
            throws Exception {
        final ResolveModuleDependenciesTask task = new ResolveModuleDependenciesTask();
        task.setModule( MockModel.dependantModule.get() );
        task.setProject( MockModel.project.get() );
        assertSetEquality( "Model dependency resolution failed.", Collections.singleton( MockModel.dependee.get() ),
                task.resolveModules() );
    }
}
