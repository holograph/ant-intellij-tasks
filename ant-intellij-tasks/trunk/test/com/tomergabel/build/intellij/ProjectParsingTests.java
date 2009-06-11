package com.tomergabel.build.intellij;

import static com.tomergabel.util.TestUtils.assertSetEquality;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ProjectParsingTests {
    @Test
    public void testProjectParsing() throws IOException, ParseException {
        final File resource = new File( this.getClass().getResource( "parsing-test.ipr" ).getFile() );
        final Project project = Project.parse( resource );

        assertEquals( "Project name incorrectly parsed.", "Delver", project.getName() );
        assertEquals( "Relative paths flag incorrectly parsed.", true, project.isRelativePaths() );
        assertEquals( "Output URL incorrectly parsed.", "file://$PROJECT_DIR$/out", project.getOutputUrl() );

        assertSetEquality( "Project modules incorrectly parsed.", new String[] {
                "file://$PROJECT_DIR$/libraries/libraries.iml",
                "file://$PROJECT_DIR$/lucene/lucene.iml",
                "file://$PROJECT_DIR$/shci-commons/shci-commons.iml"
        }, project.getModules() );

        assertSetEquality( "Project libraries incorrectly parsed.", new String[] {
                "servlet-api", "log4j", "junit" }, project.getLibraries().keySet() );
    }
}
