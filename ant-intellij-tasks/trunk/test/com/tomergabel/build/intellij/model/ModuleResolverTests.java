package com.tomergabel.build.intellij.model;

import static com.tomergabel.build.intellij.model.MockModel.Modules;
import static com.tomergabel.build.intellij.model.MockModel.Projects;
import static com.tomergabel.util.TestUtils.assertSetEquality;
import com.tomergabel.util.UriUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ModuleResolverTests {

    // ------------------------------------------------------
    // resolveUriString tests
    // ------------------------------------------------------
    
    public URI resolveUri( final Project project, final Module module, final String uriString ) throws ResolutionException {
        final PropertyResolver resolver =
                module != null ? new ModuleResolver( project, module ) : new ProjectResolver( project );
        return resolver.resolveUriString( uriString );
    }

    @Test
    public void testResolveUri_ProjectRelativeUriWithProjectSpecified_UriResolvedCorrectly() throws Exception {
        assertEquals( "Project-relative URI incorrectly resolved.",
                Projects.allModules.get().getProjectRoot().resolve( "file.ext" ),
                resolveUri( Projects.allModules.get(), null, "file://$PROJECT_DIR$/file.ext" ) );
    }

    @Test
    public void testResolveUri_ModuleRelatedUri_UriResolvedCorrectly() throws Exception {
        assertEquals( "Project-relative URI incorrectly resolved.",
                Modules.selfContained.get().getModuleRoot().resolve( "file.ext" ),
                resolveUri( null, Modules.selfContained.get(), "file://$MODULE_DIR$/file.ext" ) );
    }

    @Test
    public void testResolveUri_ProjectRelativeJarUriWithProjectSpecified_UriResolvedCorrectly() throws Exception {
        assertEquals( "Project-relative URI incorrectly resolved.",
                Projects.allModules.get().getProjectRoot().resolve( "some.jar" ),
                resolveUri( Projects.allModules.get(), null, "jar://$PROJECT_DIR$/some.jar!/" ) );
    }

    @Test
    public void testResolveUri_ModuleRelatedJarUri_UriResolvedCorrectly() throws Exception {
        assertEquals( "Project-relative URI incorrectly resolved.",
                Modules.selfContained.get().getModuleRoot().resolve( "some.jar" ),
                resolveUri( null, Modules.selfContained.get(), "jar://$MODULE_DIR$/some.jar!/" ) );
    }

    @Test
    public void testResolveUri_InlineRelativePathSpecified_UriResolvedCorrectly() throws Exception {
        assertEquals( "Inline relative URI incorrectly resolved.",
                this.getClass().getResource( "." ).toURI(),
                resolveUri( null, Modules.selfContained.get(), "jar://$MODULE_DIR$/../" ) );
    }

    // ------------------------------------------------------
    // Specific property resolution tests
    // ------------------------------------------------------

    @Test
    public void testProjectDirectoryResolutionFailure_ModuleOnly() throws Exception {
        try {
            resolveUri( null, Modules.selfContained.get(), "file://$PROJECT_DIR$/" );
            fail( "Project not specified, ResolutionException expected" );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testProjectDirectoryResolution_ModuleAndProject() throws Exception {
        assertEquals( "Project directory expanded incorrectly.", Projects.allModules.get().getProjectRoot(),
                resolveUri( Projects.allModules.get(), Modules.selfContained.get(),
                        "file://$PROJECT_DIR$/" ) );
    }

    @Test
    public void testModuleDirectoryResolution_ModuleOnly() throws Exception {
        assertEquals( "Module directory expanded incorrectly.", Modules.selfContained.get().getModuleRoot(),
                resolveUri( null, Modules.selfContained.get(), "file://$MODULE_DIR$/" ) );
    }

    @Test
    public void testModuleDirectoryResolution_ModuleAndProject() throws Exception {
        assertEquals( "Project directory expanded incorrectly.", Modules.selfContained.get().getModuleRoot(),
                resolveUri( Projects.allModules.get(), Modules.selfContained.get(), "file://$MODULE_DIR$/" ) );
    }

    // ------------------------------------------------------
    // resolveModuleDependencies tests
    // ------------------------------------------------------

    @Test
    public void testResolveModuleDependencies_WithNoProject_ResolutionExceptionIsThrown() throws Exception {
        try {
            new ModuleResolver( (ProjectResolver) null, Modules.dependantModule.get() ).resolveModuleDependencies();
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testResolveModuleDependencies_WithProjectFile_ModulesResolvedCorrectly() throws Exception {
        assertSetEquality( "Module dependencies resolved incorrectly.", Collections.singleton( Modules.dependee.get() ),
                new ModuleResolver( Projects.allModules.get(),
                        Modules.dependantModule.get() ).resolveModuleDependencies() );
    }

    // ------------------------------------------------------
    // resolveLibraryDependencies tests
    // ------------------------------------------------------

    @Test
    public void testResolveLibraryDependencies_WithProjectFile_ProjectLevelLibrariesResolvedCorrectly()
            throws Exception {
        assertSetEquality( "Project dependencies resolved incorrectly.",
                Collections.singleton( MockModel.junitLibraryPath.get() ),
                new ModuleResolver( Projects.allModules.get(),
                        Modules.dependantLibrary.get() ).resolveLibraryDependencies() );
    }

    @Test
    public void testResolveLibraryDependencies_WithModuleLibraries_LibrariesResolvedCorrectly()
            throws Exception {
        assertSetEquality( "Project dependencies resolved incorrectly.", Arrays.asList(
                UriUtils.getPath( MockModel.class.getResource( "." ).toURI().resolve( "modules/library/" ) ) ),
                new ModuleResolver( (ProjectResolver) null,
                        Modules.withModuleLibrary.get() ).resolveLibraryDependencies() );
    }

    // ------------------------------------------------------
    // resolveModuleClasspath tests
    // ------------------------------------------------------

    private Collection<String> resolveModuleClasspath( final Project project, final Module module )
            throws ResolutionException {
        return new ModuleResolver( project, module ).resolveModuleClasspath();
    }

    @Test
    public void testResolveClasspath_NoProjectLevelModuleOrLibraryDependenciesAndNoProjectSpecified_ClasspathResolvedCorrectly()
            throws Exception {
        assertSetEquality( "Classpath resolved incorrectly.", new String[] { },
                resolveModuleClasspath( null, Modules.selfContained.get() ) );
    }

    @Test
    public void testResolveClasspath_WithProjectLevelModuleDependenciesAndNoProjectSpecified_ResolutionExceptionIsThrown()
            throws Exception {
        try {
            resolveModuleClasspath( null, Modules.dependantModule.get() );
            fail( "Project not specified but module dependencies exist, expected ResolutionException" );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testResolveClasspath_WithProjectLevelModuleDependencies_ClasspathResolvedCorrectly() throws Exception {
        final String dependeeOutput = UriUtils
                .getPath( resolveUri( Projects.allModules.get(), Modules.dependee.get(),
                        Modules.dependee.get().getOutputUrl() ) );
        assertSetEquality( "Classpath resolved incorrectly.", new String[] {
                dependeeOutput, MockModel.junitLibraryPath.get()  // Inherited from dependee
        }, resolveModuleClasspath( Projects.allModules.get(), Modules.dependantModule.get() ) );
    }

    @Test
    public void testResolveClasspath_WithProjectLevelLibraryDependencies_ClasspathResolvedCorrectly()
            throws Exception {
        assertSetEquality( "Classpath resolved incorrectly.", Collections.singleton( MockModel.junitLibraryPath.get() ),
                resolveModuleClasspath( Projects.allModules.get(), Modules.dependantLibrary.get() ) );
    }

    @Test
    public void testResolveClasspath_WithProjectLevelModuleAndLibraryDependencies_ClasspathResolvedCorrectly()
            throws Exception {
        final String dependeeOutput = UriUtils.getPath(
                resolveUri( Projects.allModules.get(), Modules.dependee.get(),
                        Modules.dependee.get().getOutputUrl() ) );
        assertSetEquality( "Classpath resolved incorrectly.",
                new String[] { dependeeOutput, MockModel.junitLibraryPath.get() },
                resolveModuleClasspath( Projects.allModules.get(), Modules.dependantBoth.get() ) );
    }

    @Test
    public void testResolveClasspath_WithProjectLevelLibraryDependenciesAndNoProjectSpecified_ResolutionExceptionIsThrown()
            throws Exception {
        try {
            resolveModuleClasspath( null, Modules.dependantLibrary.get() );
            fail( "Project not specified but project level library dependencies exist, expected ResolutionException" );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testResolveClasspath_WithModuleLevelLibraryDependenciesAndNoProjectSpecified_ClasspathResolvedCorrectly()
            throws Exception {
        assertSetEquality( "Classpath resolved incorrectly.", Collections.singleton(
                UriUtils.getPath( MockModel.class.getResource( "." ).toURI().resolve( "modules/library/" ) ) ),
                resolveModuleClasspath( null, Modules.withModuleLibrary.get() ) );
    }

    @Test
    public void testResolveClasspath_WithModuleLevelLibraryDependenciesWithJarDirectories_ClasspathResolvedCorrectly()
            throws Exception {
        assertSetEquality( "Classpath resolved incorrectly.", Collections.singleton( MockModel.Jars.outerMock ),
                resolveModuleClasspath( null, Modules.withJarDirectory.get() ) );
    }

    @Test
    public void testResolveClasspath_WithModuleLevelLibraryDependenciesWithRecursiveJarDirectories_ClasspathResolvedCorrectly()
            throws Exception {
        assertSetEquality( "Classpath resolved incorrectly.",
                Arrays.asList( MockModel.Jars.innerMock, MockModel.Jars.outerMock ),
                resolveModuleClasspath( null, Modules.withJarDirectoryRecursive.get() ) );
    }
}
