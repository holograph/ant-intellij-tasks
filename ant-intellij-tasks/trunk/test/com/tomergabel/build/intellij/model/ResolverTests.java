package com.tomergabel.build.intellij.model;

import static com.tomergabel.build.intellij.model.MockModel.*;
import static com.tomergabel.build.intellij.model.MockModel.Projects.*;
import static com.tomergabel.build.intellij.model.MockModel.Modules.*;
import static com.tomergabel.util.TestUtils.assertSetEquality;
import com.tomergabel.util.UriUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.util.Collections;

public class ResolverTests {

    // ------------------------------------------------------
    // resolveUriString tests
    // ------------------------------------------------------

    @Test
    public void testResolveUri_ProjectRelativeUriWithProjectSpecified_UriResolvedCorrectly() throws Exception {
        assertEquals( "Project-relative URI incorrectly resolved.",
                allModules.get().getProjectRoot().resolve( "file.ext" ),
                Resolver.resolveUri( allModules.get(), null, "file://$PROJECT_DIR$/file.ext" ) );
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
                selfContained.get().getModuleRoot().resolve( "file.ext" ),
                Resolver.resolveUri( null, selfContained.get(), "file://$MODULE_DIR$/file.ext" ) );
    }

    @Test
    public void testResolveUri_ProjectRelativeJarUriWithProjectSpecified_UriResolvedCorrectly() throws Exception {
        assertEquals( "Project-relative URI incorrectly resolved.",
                allModules.get().getProjectRoot().resolve( "some.jar" ),
                Resolver.resolveUri( allModules.get(), null, "jar://$PROJECT_DIR$/some.jar!/" ) );
    }

    @Test
    public void testResolveUri_ModuleRelatedJarUri_UriResolvedCorrectly() throws Exception {
        assertEquals( "Project-relative URI incorrectly resolved.",
                selfContained.get().getModuleRoot().resolve( "some.jar" ),
                Resolver.resolveUri( null, selfContained.get(), "jar://$MODULE_DIR$/some.jar!/" ) );
    }

    @Test
    public void testResolveUri_InlineRelativePathSpecified_UriResolvedCorrectly() throws Exception {
        assertEquals( "Inline relative URI incorrectly resolved.",
                this.getClass().getResource( "." ).toURI(),
                Resolver.resolveUri( null, selfContained.get(), "jar://$MODULE_DIR$/../" ) );
    }

    // ------------------------------------------------------
    // Specific property resolution tests
    // ------------------------------------------------------

    @Test
    public void testProjectDirectoryResolutionFailure_ModuleOnly() throws Exception {
        try {
            Resolver.resolveUri( null, selfContained.get(), "file://$PROJECT_DIR$/" );
            fail( "Project not specified, ResolutionException expected" );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testProjectDirectoryResolution_ModuleAndProject() throws Exception {
        assertEquals( "Project directory expanded incorrectly.", allModules.get().getProjectRoot(),
                Resolver.resolveUri( allModules.get(), selfContained.get(), "file://$PROJECT_DIR$/" ) );
    }

    @Test
    public void testModuleDirectoryResolution_ModuleOnly() throws Exception {
        assertEquals( "Module directory expanded incorrectly.", selfContained.get().getModuleRoot(),
                Resolver.resolveUri( null, selfContained.get(), "file://$MODULE_DIR$/" ) );
    }

    @Test
    public void testModuleDirectoryResolution_ModuleAndProject() throws Exception {
        assertEquals( "Project directory expanded incorrectly.", selfContained.get().getModuleRoot(),
                Resolver.resolveUri( allModules.get(), selfContained.get(), "file://$MODULE_DIR$/" ) );
    }

    // ------------------------------------------------------
    // resolveModuleDependencies tests
    // ------------------------------------------------------

    @Test
    public void testResolveModuleDependencies_WithNoProject_ResolutionExceptionIsThrown() throws Exception {
        try {
            Resolver.resolveModuleDependencies( null, dependantModule.get() );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testResolveModuleDependencies_WithProjectFile_ModulesResolvedCorrectly() throws Exception {
        assertSetEquality( "Module dependencies resolved incorrectly.", Collections.singleton( dependee.get() ),
                Resolver.resolveModuleDependencies( allModules.get(), dependantModule.get() ) );
    }

    // ------------------------------------------------------
    // resolveLibraryDependencies tests
    // ------------------------------------------------------

    @Test
    public void testResolveLibraryDependencies_WithProjectFile_ProjectLevelLibrariesResolvedCorrectly()
            throws Exception {
        assertSetEquality( "Project dependencies resolved incorrectly.",
                Collections.singleton( junitLibraryPath.get() ),
                Resolver.resolveLibraryDependencies( allModules.get(), dependantLibrary.get() ) );
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
                Resolver.resolveClasspath( null, selfContained.get() ) );
    }

    @Test
    public void testResolveClasspath_WithProjectLevelModuleDependenciesAndNoProjectSpecified_ResolutionExceptionIsThrown()
            throws Exception {
        try {
            Resolver.resolveClasspath( null, dependantModule.get() );
            fail( "Project not specified but module dependencies exist, expected ResolutionException" );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testResolveClasspath_WithProjectLevelModuleDependencies_ClasspathResolvedCorrectly() throws Exception {
        final String dependeeOutput = UriUtils
                .getPath( Resolver.resolveUri( allModules.get(), dependee.get(), dependee.get().getOutputUrl() ) );
        assertSetEquality( "Classpath resolved incorrectly.", new String[] {
                dependeeOutput,
                junitLibraryPath.get()  // Inherited from dependee
        }, Resolver.resolveClasspath( allModules.get(), dependantModule.get() ) );
    }

    @Test
    public void testResolveClasspath_WithProjectLevelLibraryDependencies_ClasspathResolvedCorrectly()
            throws Exception {
        assertSetEquality( "Classpath resolved incorrectly.", Collections.singleton( junitLibraryPath.get() ),
                Resolver.resolveClasspath( allModules.get(), dependantLibrary.get() ) );
    }

    @Test
    public void testResolveClasspath_WithProjectLevelModuleAndLibraryDependencies_ClasspathResolvedCorrectly()
            throws Exception {
        final String dependeeOutput = UriUtils
                .getPath( Resolver.resolveUri( allModules.get(), dependee.get(), dependee.get().getOutputUrl() ) );
        assertSetEquality( "Classpath resolved incorrectly.", new String[] { dependeeOutput, junitLibraryPath.get() },
                Resolver.resolveClasspath( allModules.get(), dependantBoth.get() ) );
    }

    @Test
    public void testResolveClasspath_WithProjectLevelLibraryDependenciesAndNoProjectSpecified_ResolutionExceptionIsThrown()
            throws Exception {
        try {
            Resolver.resolveClasspath( null, dependantLibrary.get() );
            fail( "Project not specified but project level library dependencies exist, expected ResolutionException" );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    // --

}
