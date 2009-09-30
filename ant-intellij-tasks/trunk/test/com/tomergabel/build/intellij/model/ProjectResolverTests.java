/*
	Copyright 2009 Tomer Gabel <tomer@tomergabel.com>

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


	ant-intellij-tasks project (http://code.google.com/p/ant-intellij-tasks/)

	$Id$
*/

package com.tomergabel.build.intellij.model;

import static com.tomergabel.build.intellij.model.MockModel.Modules.*;
import com.tomergabel.util.LazyInitializationException;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

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
