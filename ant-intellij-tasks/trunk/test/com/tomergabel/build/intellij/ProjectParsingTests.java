package com.tomergabel.build.intellij;

import static com.tomergabel.util.TestUtils.assertSetEquality;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;

import java.io.File;
import java.io.IOException;

public class ProjectParsingTests {
    private File resource;
    private Project project;

    @Before
    public void testSetup() throws IOException, ParseException {
        resource = new File( this.getClass().getResource( "parsing-test.ipr" ).getFile() );
        project = Project.parse( resource );
    }

    @Test
    public void testGeneralMetadataParsing() {
        assertEquals( "Project name incorrectly parsed.", "Delver", this.project.getName() );
        assertEquals( "Relative paths flag incorrectly parsed.", true, this.project.isRelativePaths() );
        assertEquals( "Output URL incorrectly parsed.", "file://$PROJECT_DIR$/out", this.project.getOutputUrl() );
    }

    @Test
    public void testModuleParsing() {
        assertSetEquality( "Project modules incorrectly parsed.", new String[] {
                "file://$PROJECT_DIR$/libraries/libraries.iml",
                "file://$PROJECT_DIR$/lucene/lucene.iml",
                "file://$PROJECT_DIR$/shci-commons/shci-commons.iml"
        }, this.project.getModules() );
    }

    @Test
    public void testLibraryParsing() {
        // Assert correct library parsing
        assertSetEquality( "Project libraries incorrectly parsed.", new String[] {
                "servlet-api", "log4j", "junit"
        }, this.project.getLibraries().keySet() );
        // -- servlet-api
        assertSetEquality( "Project sources incorrect parsed.",
                new String[] { "jar://$PROJECT_DIR$/libraries/jee/servlet-api.jar!/" },
                this.project.getLibraries().get( "servlet-api" ).getClasses() );
        assertSetEquality( "Project sources incorrect parsed.",
                new String[] { "jar://$PROJECT_DIR$/libraries/jee/servlet-src.zip!/" },
                this.project.getLibraries().get( "servlet-api" ).getSources() );
        assertSetEquality( "Project sources incorrect parsed.", new String[] { },
                this.project.getLibraries().get( "servlet-api" ).getJavadoc() );
        // -- log4j
        assertSetEquality( "Project sources incorrect parsed.",
                new String[] { "jar://$PROJECT_DIR$/libraries/log4j/log4j-1.2.15.jar!/" },
                this.project.getLibraries().get( "log4j" ).getClasses() );
        assertSetEquality( "Project sources incorrect parsed.", new String[] { },
                this.project.getLibraries().get( "log4j" ).getSources() );
        assertSetEquality( "Project sources incorrect parsed.", new String[] { },
                this.project.getLibraries().get( "log4j" ).getJavadoc() );
        // -- junit
        assertSetEquality( "Project sources incorrect parsed.",
                new String[] { "jar://$PROJECT_DIR$/libraries/junit/junit-4.6.jar!/" },
                this.project.getLibraries().get( "junit" ).getClasses() );
        assertSetEquality( "Project sources incorrect parsed.",
                new String[] { "jar://$PROJECT_DIR$/libraries/junit/junit-4.6-src.jar!/" },
                this.project.getLibraries().get( "junit" ).getSources() );
        assertSetEquality( "Project sources incorrect parsed.", new String[] { },
                this.project.getLibraries().get( "junit" ).getJavadoc() );
    }

    @Test
    public void testPropertyExtraction() {
        // Assert correct property extraction
        assertSetEquality( "Project properties incorrectly generated.", new String[] { "PROJECT_DIR" },
                this.project.getProperties().keySet() );
        assertEquals( "Project directory incorrectly set.", resource.getParentFile(),
                this.project.getProperties().get( "PROJECT_DIR" ) );
    }
}
