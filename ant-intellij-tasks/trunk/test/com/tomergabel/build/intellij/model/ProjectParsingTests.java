package com.tomergabel.build.intellij.model;

import static com.tomergabel.util.TestUtils.assertSetEquality;
import com.tomergabel.util.UriUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

public class ProjectParsingTests {
    private URI resource;
    private Project project;

    @Before
    public void testSetup() throws Exception {
        this.resource = this.getClass().getResource( "parsing-test.ipr" ).toURI();
        this.project = Project.parse( this.resource );
    }

    @Test
    public void testGeneralMetadataParsing() {
        assertEquals( "Project name incorrectly parsed.", "Test", this.project.getName() );
        assertEquals( "Relative paths flag incorrectly parsed.", true, this.project.isRelativePaths() );
        assertEquals( "Output URL incorrectly parsed.", "file://$PROJECT_DIR$/out", this.project.getOutputUrl() );
        assertEquals( "Build JAR on make settings incorrectly parsed.", true, this.project.isBuildJarsOnMake() );
    }

    @Test
    public void testModuleParsing() {
        assertSetEquality( "Project test incorrectly parsed.", new String[] {
                "file://$PROJECT_DIR$/parsing-test.iml",
                "file://$PROJECT_DIR$/dependee.iml"
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
        assertEquals( "Project directory incorrectly set.", UriUtils.getParent( this.resource ).getPath(),
                this.project.getProperties().get( "PROJECT_DIR" ) );
    }

    @Test
    public void testResourceExtensionExtraction() {
        // Assert correct resource extension extraction
        assertSetEquality( "Project resource extensions incorrectly extracted.",
                new String[] { "properties", "xml", "html", "dtd", "tld", "gif", "png", "jpeg", "jpg" },
                this.project.getResourceExtensions() );
    }


    @Test
    public void testResourceWildcardPatternExtraction() {
        // Assert correct resource extension extraction
        assertSetEquality( "Project resource wildcard patterns incorrectly extracted.",
                new String[] {
                        "?*.properties", "?*.xml", "?*.gif", "?*.png", "?*.jpeg", "?*.jpg", "?*.html", "?*.dtd",
                        "?*.tld", "?*.ftl", "?*.properties", "?*.xml", "?*.html", "?*.dtd", "?*.tld", "?*.gif",
                        "?*.png", "?*.jpeg", "?*.jpg", "?*.zip"
                },
                this.project.getResourceWildcardPatterns() );
    }
}
