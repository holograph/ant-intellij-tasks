package com.tomergabel.build.intellij.model;

import static com.tomergabel.build.intellij.model.MockModel.Modules;
import static com.tomergabel.build.intellij.model.MockModel.Projects;
import com.tomergabel.util.LazyInitializationException;
import static com.tomergabel.util.TestUtils.assertSetEquality;
import com.tomergabel.util.UriUtils;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

public class ResolverTests {

    // ------------------------------------------------------
    // resolveUriString tests
    // ------------------------------------------------------

    @Test
    public void testResolveUri_ProjectRelativeUriWithProjectSpecified_UriResolvedCorrectly() throws Exception {
        assertEquals( "Project-relative URI incorrectly resolved.",
                Projects.allModules.get().getProjectRoot().resolve( "file.ext" ),
                Resolver.resolveUri( Projects.allModules.get(), null, "file://$PROJECT_DIR$/file.ext" ) );
    }

    @Test
    public void testResolveUri_UnknownProperty_ResolutionExceptionIsThrown() throws Exception {
        try {
            Resolver.resolveUri( null, null, "file://$PROJECT_DIR$/file.ext" );
            fail( "Unknown property specified, ResolutionException expected." );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testResolveUri_ModuleRelatedUri_UriResolvedCorrectly() throws Exception {
        assertEquals( "Project-relative URI incorrectly resolved.",
                Modules.selfContained.get().getModuleRoot().resolve( "file.ext" ),
                Resolver.resolveUri( null, Modules.selfContained.get(), "file://$MODULE_DIR$/file.ext" ) );
    }

    @Test
    public void testResolveUri_ProjectRelativeJarUriWithProjectSpecified_UriResolvedCorrectly() throws Exception {
        assertEquals( "Project-relative URI incorrectly resolved.",
                Projects.allModules.get().getProjectRoot().resolve( "some.jar" ),
                Resolver.resolveUri( Projects.allModules.get(), null, "jar://$PROJECT_DIR$/some.jar!/" ) );
    }

    @Test
    public void testResolveUri_ModuleRelatedJarUri_UriResolvedCorrectly() throws Exception {
        assertEquals( "Project-relative URI incorrectly resolved.",
                Modules.selfContained.get().getModuleRoot().resolve( "some.jar" ),
                Resolver.resolveUri( null, Modules.selfContained.get(), "jar://$MODULE_DIR$/some.jar!/" ) );
    }

    @Test
    public void testResolveUri_InlineRelativePathSpecified_UriResolvedCorrectly() throws Exception {
        assertEquals( "Inline relative URI incorrectly resolved.",
                this.getClass().getResource( "." ).toURI(),
                Resolver.resolveUri( null, Modules.selfContained.get(), "jar://$MODULE_DIR$/../" ) );
    }

    // ------------------------------------------------------
    // Specific property resolution tests
    // ------------------------------------------------------

    @Test
    public void testProjectDirectoryResolutionFailure_ModuleOnly() throws Exception {
        try {
            Resolver.resolveUri( null, Modules.selfContained.get(), "file://$PROJECT_DIR$/" );
            fail( "Project not specified, ResolutionException expected" );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testProjectDirectoryResolution_ModuleAndProject() throws Exception {
        assertEquals( "Project directory expanded incorrectly.", Projects.allModules.get().getProjectRoot(),
                Resolver.resolveUri( Projects.allModules.get(), Modules.selfContained.get(),
                        "file://$PROJECT_DIR$/" ) );
    }

    @Test
    public void testModuleDirectoryResolution_ModuleOnly() throws Exception {
        assertEquals( "Module directory expanded incorrectly.", Modules.selfContained.get().getModuleRoot(),
                Resolver.resolveUri( null, Modules.selfContained.get(), "file://$MODULE_DIR$/" ) );
    }

    @Test
    public void testModuleDirectoryResolution_ModuleAndProject() throws Exception {
        assertEquals( "Project directory expanded incorrectly.", Modules.selfContained.get().getModuleRoot(),
                Resolver.resolveUri( Projects.allModules.get(), Modules.selfContained.get(), "file://$MODULE_DIR$/" ) );
    }

    // ------------------------------------------------------
    // resolveModuleDependencies tests
    // ------------------------------------------------------

    @Test
    public void testResolveModuleDependencies_WithNoProject_ResolutionExceptionIsThrown() throws Exception {
        try {
            Resolver.resolveModuleDependencies( null, Modules.dependantModule.get() );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testResolveModuleDependencies_WithProjectFile_ModulesResolvedCorrectly() throws Exception {
        assertSetEquality( "Module dependencies resolved incorrectly.", Collections.singleton( Modules.dependee.get() ),
                Resolver.resolveModuleDependencies( Projects.allModules.get(), Modules.dependantModule.get() ) );
    }

    // ------------------------------------------------------
    // resolveLibraryDependencies tests
    // ------------------------------------------------------

    @Test
    public void testResolveLibraryDependencies_WithProjectFile_ProjectLevelLibrariesResolvedCorrectly()
            throws Exception {
        assertSetEquality( "Project dependencies resolved incorrectly.",
                Collections.singleton( MockModel.junitLibraryPath.get() ),
                Resolver.resolveLibraryDependencies( Projects.allModules.get(), Modules.dependantLibrary.get() ) );
    }

    // ------------------------------------------------------
    // resolveModuleClasspath tests
    // ------------------------------------------------------

    @Test
    public void testResolveClasspath_NullModuleFile_ThrowsResolutionException() throws Exception {
        try {
            new Resolver( null, null ).resolveModuleClasspath();
            fail( "No module specified, expected ResolutionException" );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testResolveClasspath_NoProjectLevelModuleOrLibraryDependenciesAndNoProjectSpecified_ClasspathResolvedCorrectly()
            throws Exception {
        assertSetEquality( "Classpath resolved incorrectly.", new String[] { },
                Resolver.resolveClasspath( null, Modules.selfContained.get() ) );
    }

    @Test
    public void testResolveClasspath_WithProjectLevelModuleDependenciesAndNoProjectSpecified_ResolutionExceptionIsThrown()
            throws Exception {
        try {
            Resolver.resolveClasspath( null, Modules.dependantModule.get() );
            fail( "Project not specified but module dependencies exist, expected ResolutionException" );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testResolveClasspath_WithProjectLevelModuleDependencies_ClasspathResolvedCorrectly() throws Exception {
        final String dependeeOutput = UriUtils
                .getPath( Resolver.resolveUri( Projects.allModules.get(), Modules.dependee.get(),
                        Modules.dependee.get().getOutputUrl() ) );
        assertSetEquality( "Classpath resolved incorrectly.", new String[] {
                dependeeOutput, MockModel.junitLibraryPath.get()  // Inherited from dependee
        }, Resolver.resolveClasspath( Projects.allModules.get(), Modules.dependantModule.get() ) );
    }

    @Test
    public void testResolveClasspath_WithProjectLevelLibraryDependencies_ClasspathResolvedCorrectly()
            throws Exception {
        assertSetEquality( "Classpath resolved incorrectly.", Collections.singleton( MockModel.junitLibraryPath.get() ),
                Resolver.resolveClasspath( Projects.allModules.get(), Modules.dependantLibrary.get() ) );
    }

    @Test
    public void testResolveClasspath_WithProjectLevelModuleAndLibraryDependencies_ClasspathResolvedCorrectly()
            throws Exception {
        final String dependeeOutput = UriUtils.getPath(
                Resolver.resolveUri( Projects.allModules.get(), Modules.dependee.get(),
                        Modules.dependee.get().getOutputUrl() ) );
        assertSetEquality( "Classpath resolved incorrectly.",
                new String[] { dependeeOutput, MockModel.junitLibraryPath.get() },
                Resolver.resolveClasspath( Projects.allModules.get(), Modules.dependantBoth.get() ) );
    }

    @Test
    public void testResolveClasspath_WithProjectLevelLibraryDependenciesAndNoProjectSpecified_ResolutionExceptionIsThrown()
            throws Exception {
        try {
            Resolver.resolveClasspath( null, Modules.dependantLibrary.get() );
            fail( "Project not specified but project level library dependencies exist, expected ResolutionException" );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    // ------------------------------------------------------
    // resolveModuleBuildOrder tests
    // ------------------------------------------------------

    @Test
    public void testResolveModuleBuildOrder_ProjectNotSpecified_ResolutionExceptionIsThrown() throws ResolutionException {
        try {
            new Resolver( null, null ).resolveModuleBuildOrder();
            fail( "Project not specified but module build order resolved, expected ResolutionException" );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testResolveModuleBuildOrder_ProjectSpecifiedAndModulesAvailable_BuildOrderResolvedCorrectly()
            throws ResolutionException, LazyInitializationException {
        final Collection<Module> buildOrder = new Resolver( MockModel.Projects.buildOrderTest.get(), null ).resolveModuleBuildOrder();
        assertArrayEquals( "Module build order resolved incorrectly.", new Object[] {
                Modules.buildOrderTestD.get(),
                Modules.buildOrderTestC.get(),
                Modules.buildOrderTestB.get(),
                Modules.buildOrderTestA.get()
        }, buildOrder.toArray() );
    }
}
