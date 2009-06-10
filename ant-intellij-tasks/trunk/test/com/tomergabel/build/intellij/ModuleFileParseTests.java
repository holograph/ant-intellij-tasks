package com.tomergabel.build.intellij;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class ModuleFileParseTests {

    private static <T> void assertSetEquality( Collection<T> expected, T[] actual ) {
        assertSetEquality( null, expected, actual );
    }

    private static <T> void assertSetEquality( T[] expected, Collection<T> actual ) {
        assertSetEquality( null, expected, actual );
    }

    private static <T> void assertSetEquality( T[] expected, T[] actual ) {
        assertSetEquality( null, expected, actual );
    }

    private static <T> void assertSetEquality( String message, Collection<T> expected, T[] actual ) {
        assertSetEquality( message, expected, Arrays.asList( actual ) );
    }

    private static <T> void assertSetEquality( String message, T[] expected, Collection<T> actual ) {
        assertSetEquality( message, Arrays.asList( expected ), actual );
    }

    private static <T> void assertSetEquality( String message, T[] expected, T[] actual ) {
        assertSetEquality( message, Arrays.asList( expected ), Arrays.asList( actual ) );
    }

    private static <T> void assertSetEquality( Collection<T> expected, Collection<T> actual ) {
        assertSetEquality( null, expected, actual );
    }

    private static <T> void assertSetEquality( String message, Collection<T> expected, Collection<T> actual ) {
        final HashSet<T> expectedSet = new HashSet<T>( expected );
        final HashSet<T> actualSet = new HashSet<T>( actual );
        expectedSet.removeAll( actual );
        actualSet.removeAll( expected );
        if ( expectedSet.size() == 0 && actualSet.size() == 0 )
            return;

        final StringBuilder sb = new StringBuilder();
        if ( message != null )
            sb.append( message ).append( Character.toChars( Character.LINE_SEPARATOR ) );
        sb.append( "Assertion failed: " );
        if ( expectedSet.size() > 0 ) {
            sb.append( "missing " ).append( expectedSet );
            if ( actualSet.size() > 0 )
                sb.append( ", " );
        }
        if ( actualSet.size() > 0 )
            sb.append( "unexpected " ).append( actualSet );
        throw new AssertionError( sb.toString() );
    }

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
