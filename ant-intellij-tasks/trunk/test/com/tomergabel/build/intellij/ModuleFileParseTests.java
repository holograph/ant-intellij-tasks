package com.tomergabel.build.intellij;

import static com.tomergabel.util.TestUtils.*;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.File;

public class ModuleFileParseTests {
    @Test
    public void testModuleFileParsing() throws Exception {
        final File resource = new File( this.getClass().getResource( "parsing-test.iml" ).getFile() );
        final Module module = Module.parse( resource );

        assertEquals( "Project name incorrectly parsed.", "parsing-test", module.getName() );
        assertEquals( "Compiler output URL incorrectly parsed.", "file://$MODULE_DIR$/bin", module.getOutputUrl() );
        assertEquals( "Compiler test class output URL incorrectly parsed.", "file://$MODULE_DIR$/bin",
                module.getTestOutputUrl() );
        assertEquals( "Content root URL incorrectly parsed.", "file://$MODULE_DIR$", module.getContentRootUrl() );
        assertSetEquality( "Source URLs incorrectly parsed.", new String[] { "file://$MODULE_DIR$/src" },
                module.getSourceUrls() );
        assertSetEquality( "Test source URLs incorrectly parsed.", new String[] { "file://$MODULE_DIR$/test" },
                module.getTestSourceUrls() );
        assertSetEquality( "Dependencies incorrectly parsed.", new Dependency[] {
                new LibraryDependency( LibraryDependency.Scope.PROJECT, "servlet-api" ),
                new LibraryDependency( LibraryDependency.Scope.PROJECT, "log4j" ),
                new LibraryDependency( LibraryDependency.Scope.PROJECT, "junit" ),
                new ModuleDependency( "lucene" ),
                new ModuleDependency( "shci-commons" )
        }, module.getDepdencies() );
    }
}
