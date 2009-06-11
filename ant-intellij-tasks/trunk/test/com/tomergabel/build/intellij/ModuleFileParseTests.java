package com.tomergabel.build.intellij;

import static com.tomergabel.util.TestUtils.assertSetEquality;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ModuleFileParseTests {
    private File resource;
    private Module module;

    @Before
    public void testSetup() throws IOException, ParseException {
        this.resource = new File( this.getClass().getResource( "parsing-test.iml" ).getFile() );
        this.module = Module.parse( this.resource );
    }

    @Test
    public void testPropertyExtraction() {
        assertSetEquality( "Module properties incorrectly generated.", new String[] { "MODULE_DIR" },
                this.module.getProperties().keySet() );
        assertEquals( "Module directory incorrectly set.", resource.getParentFile(),
                this.module.getProperties().get( "MODULE_DIR" ) );
    }

    @Test
    public void testDependencyExtraction() {
        assertSetEquality( "Dependencies incorrectly parsed.", new Dependency[] {
                new LibraryDependency( LibraryDependency.Scope.PROJECT, "servlet-api" ),
                new LibraryDependency( LibraryDependency.Scope.PROJECT, "log4j" ),
                new LibraryDependency( LibraryDependency.Scope.PROJECT, "junit" ),
                new ModuleDependency( "lucene" ),
                new ModuleDependency( "shci-commons" )
        }, this.module.getDepdencies() );
    }

    @Test
    public void testSourceAndTestDirectoryExtraction() {
        assertEquals( "Content root URL incorrectly parsed.", "file://$MODULE_DIR$", this.module.getContentRootUrl() );
        assertSetEquality( "Source URLs incorrectly parsed.", new String[] { "file://$MODULE_DIR$/src" },
                this.module.getSourceUrls() );
        assertSetEquality( "Test source URLs incorrectly parsed.", new String[] { "file://$MODULE_DIR$/test" },
                this.module.getTestSourceUrls() );
    }

    @Test
    public void testGeneralMetadataExtraction() {
        assertEquals( "Module name incorrectly parsed.", "parsing-test", this.module.getName() );
        assertEquals( "Compiler output URL incorrectly parsed.", "file://$MODULE_DIR$/bin",
                this.module.getOutputUrl() );
        assertEquals( "Compiler test class output URL incorrectly parsed.", "file://$MODULE_DIR$/bin",
                this.module.getTestOutputUrl() );
    }
}
