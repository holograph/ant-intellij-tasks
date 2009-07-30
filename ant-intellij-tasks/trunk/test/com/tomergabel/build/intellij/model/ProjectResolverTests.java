package com.tomergabel.build.intellij.model;

import com.tomergabel.util.LazyInitializationException;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.util.Collection;

public class ProjectResolverTests {
    // ------------------------------------------------------
    // resolveModuleBuildOrder tests
    // ------------------------------------------------------

    @Test
    public void testResolveModuleBuildOrder_ProjectSpecifiedAndModulesAvailable_BuildOrderResolvedCorrectly()
            throws ResolutionException, LazyInitializationException {
        final Collection<Module> buildOrder = new ProjectResolver( MockModel.Projects.buildOrderTest.get() )
                .resolveModuleBuildOrder();
        assertArrayEquals( "Module build order resolved incorrectly.", new Object[] {
                MockModel.Modules.buildOrderTestD.get(),
                MockModel.Modules.buildOrderTestC.get(),
                MockModel.Modules.buildOrderTestB.get(),
                MockModel.Modules.buildOrderTestA.get()
        }, buildOrder.toArray() );
    }

    @Test
    public void testResolveModuleBuildOrder_CircularDependencyPresent_ResolutionExceptionIsThrown()
            throws ResolutionException, LazyInitializationException {
        try {
            new ProjectResolver( MockModel.Projects.circularDependencyTest.get() ).resolveModuleBuildOrder();
            fail( "Project with circularly-dependent modules resolved correctly, ResolutionException expected." );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }
}
