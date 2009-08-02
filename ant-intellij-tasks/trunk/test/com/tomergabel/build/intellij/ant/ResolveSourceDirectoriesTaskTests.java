package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.MockModel;
import com.tomergabel.util.LazyInitializationException;
import static com.tomergabel.util.TestUtils.assertSetEquality;
import com.tomergabel.util.UriUtils;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;

public class ResolveSourceDirectoriesTaskTests {
    private ResolveSourceDirectoriesTask task;
    private Project project;

    static String sourceDirectory;
    static String testDirectory;

    @BeforeClass
    public static void fixtureSetup() throws URISyntaxException {
        sourceDirectory = UriUtils.getPath( MockModel.class.getResource( "." ).toURI().resolve( "modules/src" ) );
        testDirectory = UriUtils.getPath( MockModel.class.getResource( "." ).toURI().resolve( "modules/test" ) );
    }

    @Before
    public void testSetup() throws LazyInitializationException {
        this.task = new ResolveSourceDirectoriesTask();
        task.setProject( this.project = new Project() );
        task.setModule( MockModel.Modules.outputModuleRelative.get() );
    }

    @Test
    public void execute_OutputProjectRelativeButProjectNotSpecified_BuildExceptionIsThrown()
            throws URISyntaxException, LazyInitializationException {
        try {
            task.execute();
            fail( "Resolution did not fail even though project file was not specified and " +
                    "source directory is project-relative." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void execute_OutputProjectRelativeButProjectNotSpecified_NoFailOnError_NothingHappens()
            throws URISyntaxException, LazyInitializationException {
        task.setModule( MockModel.Modules.outputProjectRelative.get() );
        task.setProperty( "property" );
        task.setFailonerror( false );
        task.execute();
        assertNull( "Property generated despite failure.", this.project.getProperty( "property" ) );
    }

    @Test
    public void execute_PropertyAndPathIDNotSpecified_BuildExceptionIsThrown() throws LazyInitializationException {
        try {
            task.execute();
            fail( "Resolution did not fail even though neither property nor path ID were specified." );
        } catch ( BuildException e ) {
            // Expected, all is well
        }
    }

    @Test
    public void execute_PropertyAndPathIDNotSpecified_NoFailOnError_NothingHappens()
            throws LazyInitializationException {
        task.setFailonerror( false );
        task.execute();
    }

    @Test
    public void execute_ModuleWithTestFilesAndSourceFilterSpecified_Property_OnlySourceDirectoriesReturned()
            throws LazyInitializationException {
        task.setProperty( "property" );
        task.setFilter( Filter.source );
        task.execute();
        assertEquals( "Directories resolved incorrectly.", sourceDirectory, project.getProperty( "property" ) );
    }

    @Test
    public void execute_ModuleWithTestFilesAndTestFilterSpecified_Property_OnlyTestDirectoriesReturned()
            throws LazyInitializationException {
        task.setProperty( "property" );
        task.setFilter( Filter.test );
        task.execute();
        assertEquals( "Directories resolved incorrectly.", testDirectory, project.getProperty( "property" ) );
    }

    @Test
    public void execute_ModuleWithTestFilesAndBothFilterSpecified_Property_SourceAndTestDirectoriesReturned()
            throws LazyInitializationException {
        task.setProperty( "property" );
        task.setFilter( Filter.both );
        task.execute();
        assertSetEquality( "Directories resolved incorrectly.", Arrays.asList( sourceDirectory, testDirectory ),
                project.getProperty( "property" ).split( "," ) );
    }

    @Test
    public void execute_ModuleWithTestFilesAndSourceFilterSpecified_PathID_OnlySourceDirectoriesReturned()
            throws LazyInitializationException {
        task.setPathId( "path" );
        task.setFilter( Filter.source );
        task.execute();
        assertNotNull( "Path was not generated.", project.getReference( "path" ) );
        assertTrue( "Object generated does not derive from an Ant path.",
                project.getReference( "path" ) instanceof Path );
        final Path path = (Path) project.getReference( "path" );
        assertSetEquality( "Directories resolved incorrectly.", Collections.singleton( sourceDirectory ), path.list() );
    }

    private void assertPath( final String... directories ) {
        assertNotNull( "Path was not generated.", project.getReference( "path" ) );
        assertTrue( "Object generated does not derive from an Ant path.",
                project.getReference( "path" ) instanceof Path );
        final Path path = (Path) project.getReference( "path" );
        assertSetEquality( "Directories resolved incorrectly.", Arrays.asList( directories ), path.list() );
    }


    @Test
    public void execute_ModuleWithTestFilesAndTestFilterSpecified_PathID_OnlyTestDirectoriesReturned() {
        task.setPathId( "path" );
        task.setFilter( Filter.test );
        task.execute();
        assertPath( testDirectory );
    }

    @Test
    public void execute_ModuleWithTestFilesAndBothFilterSpecified_PathID_SourceAndTestDirectoriesReturned() {
        task.setPathId( "path" );
        task.setFilter( Filter.both );
        task.execute();
        assertPath( sourceDirectory, testDirectory );
    }

    @Test
    public void execute_ModuleWithTestFilesAndSourceFilterSpecified_PropertyAndPathID_OnlySourceDirectoriesReturned() {
        task.setProperty( "property" );
        task.setPathId( "path" );
        task.setFilter( Filter.source );
        task.execute();
        assertPath( sourceDirectory );
        assertEquals( "Directories resolved incorrectly.", sourceDirectory, project.getProperty( "property" ) );
    }

    @Test
    public void execute_ModuleWithTestFilesAndTestFilterSpecified_PropertyAndPathID_OnlyTestDirectoriesReturned() {
        task.setProperty( "property" );
        task.setPathId( "path" );
        task.setFilter( Filter.test );
        task.execute();
        assertPath( testDirectory );
        assertEquals( "Directories resolved incorrectly.", testDirectory, project.getProperty( "property" ) );
    }

    @Test
    public void execute_ModuleWithTestFilesAndBothFilterSpecified_PropertyAndPathID_SourceAndTestDirectoriesReturned() {
        task.setProperty( "property" );
        task.setPathId( "path" );
        task.setFilter( Filter.both );
        task.execute();
        assertPath( sourceDirectory, testDirectory );
        assertSetEquality( "Directories resolved incorrectly.", Arrays.asList( sourceDirectory, testDirectory ),
                project.getProperty( "property" ).split( "," ) );
    }
}
