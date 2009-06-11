package com.tomergabel.build.intellij;

import static com.tomergabel.util.TestUtils.assertSetEquality;
import com.tomergabel.util.UriUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

public class ModuleFileParseTests {
    private URI resource;
    private Module module;

    @Before
    public void testSetup() throws Exception {
        this.resource = this.getClass().getResource( "parsing-test.iml" ).toURI();
        this.module = Module.parse( this.resource );
    }

    @Test
    public void testPropertyExtraction() {
        assertSetEquality( "Module properties incorrectly generated.", new String[] { "MODULE_DIR" },
                this.module.getProperties().keySet() );
        assertEquals( "Module directory incorrectly set.", UriUtils.getParent( this.resource ).getPath(),
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
