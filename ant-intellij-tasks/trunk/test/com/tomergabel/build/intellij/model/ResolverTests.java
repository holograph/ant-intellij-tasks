package com.tomergabel.build.intellij.model;

import static com.tomergabel.util.TestUtils.assertSetEquality;
import com.tomergabel.util.UriUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ResolverTests {
    // Support code --

    URI projectUri, moduleUri;
    Project project;
    Module module;

    void loadProject() throws IOException, ParseException, URISyntaxException {
        this.projectUri = this.getClass().getResource( "parsing-test.ipr" ).toURI();
        this.project = Project.parse( this.projectUri );
    }

    void loadModule() throws IOException, ParseException, URISyntaxException {
        this.moduleUri = this.getClass().getResource( "parsing-test.iml" ).toURI();
        this.module = Module.parse( this.moduleUri );
    }

    // Test code --

    @Test
    public void testResolveUri_ProjectRelativeUriWithProjectSpecified_UriResolvedCorrectly() throws Exception {
        loadProject();
        assertEquals( "Project-relative URI incorrectly resolved.", this.project.getProjectRoot().resolve( "file.ext" ),
                Resolver.resolveUri( this.project, null, "file://$PROJECT_DIR$/file.ext" ) );
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
        loadModule();
        assertEquals( "Project-relative URI incorrectly resolved.", this.module.getModuleRoot().resolve( "file.ext" ),
                Resolver.resolveUri( null, this.module, "file://$MODULE_DIR$/file.ext" ) );
    }

    @Test
    public void testResolveUri_ProjectRelativeJarUriWithProjectSpecified_UriResolvedCorrectly() throws Exception {
        loadProject();
        assertEquals( "Project-relative URI incorrectly resolved.", this.project.getProjectRoot().resolve( "some.jar" ),
                Resolver.resolveUri( this.project, null, "jar://$PROJECT_DIR$/some.jar!/" ) );
    }

    @Test
    public void testResolveUri_ModuleRelatedJarUri_UriResolvedCorrectly() throws Exception {
        loadModule();
        assertEquals( "Project-relative URI incorrectly resolved.", this.module.getModuleRoot().resolve( "some.jar" ),
                Resolver.resolveUri( null, this.module, "jar://$MODULE_DIR$/some.jar!/" ) );
    }

    @Test
    public void testProjectDirectoryResolutionFailure_ModuleOnly() throws Exception {
        loadModule();
        try {
            Resolver.resolveUri( null, this.module, "file://$PROJECT_DIR$/" );
            fail( "Project not specified, ResolutionException expected" );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testProjectDirectoryResolution_ModuleAndProject() throws Exception {
        loadModule();
        loadProject();

        assertEquals( "Project directory expanded incorrectly.", UriUtils.getParent( this.projectUri ),
                Resolver.resolveUri( this.project, this.module, "file://$PROJECT_DIR$/" ) );
    }

    @Test
    public void testModuleDirectoryResolution_ModuleOnly() throws Exception {
        loadModule();
        assertEquals( "Module directory expanded incorrectly.", UriUtils.getParent( this.moduleUri ),
                Resolver.resolveUri( null, this.module, "file://$MODULE_DIR$/" ) );
    }

    @Test
    public void testModuleDirectoryResolution_ModuleAndProject() throws Exception {
        loadModule();
        loadProject();
        assertEquals( "Project directory expanded incorrectly.", UriUtils.getParent( this.projectUri ),
                Resolver.resolveUri( this.project, this.module, "file://$MODULE_DIR$/" ) );
    }

    @Test
    public void testResolveModuleDependencies_WithNoProject_ResolutionExceptionIsThrown() throws Exception {
        loadModule();
        try {
            Resolver.resolveModuleDependencies( null, this.module );
        } catch ( ResolutionException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testResolveModuleDependencies_WithProjectFile_ModulesResolvedCorrectly() throws Exception {
        loadModule();
        loadProject();

        assertSetEquality( "Module dependencies resolved incorrectly.", new Module[] {
                Module.parse( this.getClass().getResource( "dependee.iml" ).toURI() )
        }, Resolver.resolveModuleDependencies( this.project, this.module ) );
    }

    @Test
    public void testResolveLibraryDependencies_WithProjectFile_ProjectLevelLibrariesResolvedCorrectly()
            throws Exception {
        loadModule();
        loadProject();

        assertSetEquality( "Project dependencies resolved incorrectly.", new String[] {
                new File( this.project.getProjectRoot().resolve( "libraries/jee/servlet-api.jar" ) ).getAbsolutePath(),
                new File(
                        this.project.getProjectRoot().resolve( "libraries/log4j/log4j-1.2.15.jar" ) ).getAbsolutePath(),
                new File( this.project.getProjectRoot().resolve(
                        "libraries/junit/junit-4.6.jar" ) ).getAbsolutePath(),
        }, Resolver.resolveLibraryDependencies( this.project, this.module ) );
    }
}