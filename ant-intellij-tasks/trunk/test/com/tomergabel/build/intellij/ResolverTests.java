package com.tomergabel.build.intellij;

import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

public class ResolverTests {
    // Support code --

    File projectFile, moduleFile;
    Project project;
    Module module;

    void loadProject() throws IOException, ParseException {
        this.projectFile = new File( this.getClass().getResource( "parsing-test.ipr" ).getFile() );
        this.project = Project.parse( this.projectFile );
    }

    void loadModule() throws IOException, ParseException {
        this.moduleFile = new File( this.getClass().getResource( "parsing-test.iml" ).getFile() );
        this.module = Module.parse( this.moduleFile );
    }

    // Test code --

    @Test
    public void testProjectDirectoryResolutionFailure_ModuleOnly() throws IOException, ParseException {
        loadModule();
        try {
            Resolver.resolve( null, this.module, "file://$PROJECT_DIR$/" );
            fail( "Project not specified, PropertyResolutionException expected" );
        } catch ( PropertyResolutionException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void testProjectDirectoryResolution_ModuleAndProject()
            throws IOException, ParseException, PropertyResolutionException {
        loadModule();
        loadProject();
        assertEquals( "Project directory expanded incorrectly.", "file://" + this.projectFile.getParent() + "/",
                Resolver.resolve( this.project, this.module, "file://$PROJECT_DIR$/" ) );
    }

    @Test
    public void testModuleDirectoryResolution_ModuleOnly() throws Exception {
        loadModule();
        assertEquals( "Module directory expanded incorrectly.", "file://" + this.moduleFile.getParent() + "/",
                Resolver.resolve( null, this.module, "file://$MODULE_DIR$/" ) );
    }

    @Test
    public void testModuleDirectoryResolution_ModuleAndProject()
            throws IOException, ParseException, PropertyResolutionException {
        loadModule();
        loadProject();
        assertEquals( "Project directory expanded incorrectly.", "file://" + this.moduleFile.getParent() + "/",
                Resolver.resolve( this.project, this.module, "file://$MODULE_DIR$/" ) );
    }
}
