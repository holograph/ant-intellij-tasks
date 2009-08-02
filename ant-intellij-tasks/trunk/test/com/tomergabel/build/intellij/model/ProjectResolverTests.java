package com.tomergabel.build.intellij.model;

import com.tomergabel.util.LazyInitializationException;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import static com.tomergabel.build.intellij.model.MockModel.Modules.*;

import java.util.Arrays;
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
                buildOrderTestD.get(),
                buildOrderTestC.get(),
                buildOrderTestB.get(),
                buildOrderTestA.get()
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

    @Test
    public void testResolveModuleBuildOrderByNames_ProjectSpecifiedAndModulesAvailable_PartialBuildOrderResolvedCorrectly()
            throws ResolutionException, LazyInitializationException {
        final Collection<Module> buildOrder = new ProjectResolver( MockModel.Projects.buildOrderTest.get() )
                .resolveModuleBuildOrder( Arrays.asList( buildOrderTestA.get(), buildOrderTestB.get() ) );
        assertArrayEquals( "Module build order resolved incorrectly", new Object[] {
                buildOrderTestB.get(),
                buildOrderTestA.get()
        }, buildOrder.toArray() );
    }
}
