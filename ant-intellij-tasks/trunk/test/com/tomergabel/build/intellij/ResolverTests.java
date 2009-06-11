package com.tomergabel.build.intellij;

import com.tomergabel.util.UriUtils;
import static com.tomergabel.util.TestUtils.assertSetEquality;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

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
    public void testResolveModuleDependencies_WithNoProject_IllegalArgumentExceptionIsThrown() throws Exception {
        loadModule();
        try {
            Resolver.resolveModuleDependencies( null, this.module );
        } catch ( IllegalArgumentException e ) {
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
}
