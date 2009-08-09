package com.tomergabel.build.intellij.model;

import com.tomergabel.util.LazyInitializationException;
import static com.tomergabel.util.TestUtils.assertSetEquality;
import com.tomergabel.util.UriUtils;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class ModuleParsingTests {
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
                new ProjectLibraryDependency( "servlet-api" ),
                new ProjectLibraryDependency( "log4j" ),
                new ProjectLibraryDependency( "junit" ),
                new ModuleDependency( "dependee" )
        }, this.module.getDependencies() );
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

    @Test
    public void testOutputInheritenceExtraction() {
        assertEquals( "Module output inheritence incorrectly parsed.", true, this.module.isOutputInherited() );
    }

    @Test
    public void testJarSettingsExtraction() throws LazyInitializationException, URISyntaxException {
        final Module module = MockModel.Modules.jarOutputSelfContained.get();
        assertNotNull( "Module JAR settings were not extracted.", module.getJarSettings() );
        assertEquals( "Module JAR output URL incorrectly parsed.", "file://$MODULE_DIR$/out/test.jar",
                module.getJarSettings().getJarUrl() );
        assertEquals( "Module JAR output main class incorrectly parsed.", "test.class",
                module.getJarSettings().getMainClass() );
        assertEquals( "Module JAR output resource list incorrectly parsed.", 1,
                module.getJarSettings().getModuleOutputs().size() );
        final Module.ModuleOutputContainer output = module.getJarSettings().getModuleOutputs().iterator().next();
        assertNotNull( "Module JAR output resource list contains null module.", output );
        assertEquals( "Module JAR output resource list incorrectly parsed: incorrect module name,",
                "jar-output-self-contained", output.getModuleName() );
        assertEquals( "Module JAR output resource list incorrectly parsed: incorrect packaging method.",
                PackagingMethod.COPY, output.getPackaging() );
        assertEquals( "Module JAR output resource list incorrectly parsed: incorrect target URI,",
                new URI( "/" ), output.getTargetUri() );
    }

    @Test
    public void testWarSettingsExtraction() {
//        final Module module = MockModel.Modules.jarOutputSelfContained.get();
//        assertNotNull( "Module JAR settings were not extracted.", module.getJarSettings() );
//        assertEquals( "Module JAR output URL incorrectly parsed.", "file://$MODULE_DIR$/out/test.jar",
//                module.getJarSettings().getJarUrl() );
//        assertEquals( "Module JAR output main class incorrectly parsed.", "test.class",
//                module.getJarSettings().getMainClass() );
//        assertEquals( "Module JAR output resource list incorrectly parsed.", 1,
//                module.getJarSettings().getModuleOutputs().size() );
//        final Module.ModuleOutputContainer output = module.getJarSettings().getModuleOutputs().iterator().next();
//        assertNotNull( "Module JAR output resource list contains null module.", output );
//        assertEquals( "Module JAR output resource list incorrectly parsed: incorrect module name,",
//                "jar-output-self-contained", output.getModuleName() );
//        assertEquals( "Module JAR output resource list incorrectly parsed: incorrect packaging method.",
//                Module.PackagingMethod.COPY, output.getPackaging() );
//        assertEquals( "Module JAR output resource list incorrectly parsed: incorrect target URI,",
//                new URI( "/" ), output.getTargetUri() );
    }
}
