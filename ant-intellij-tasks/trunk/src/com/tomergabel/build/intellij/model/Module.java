package com.tomergabel.build.intellij.model;

import static com.tomergabel.util.CollectionUtils.deepHashCode;
import static com.tomergabel.util.CollectionUtils.setEquals;
import com.tomergabel.util.UriUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public final class Module extends IntelliJParserBase {
    private final URI moduleDescriptor;
    private final Collection<String> sourceUrls;
    private final Collection<String> testSourceUrls;
    private final Collection<Dependency> dependencies;
    private final String name;

    private String outputUrl = null;
    private String testOutputUrl = null;
    private String contentRootUrl;
    private boolean inheritOutput = true;
    private JarSettings jarSettings = null;

    public interface JarSettings {
        String getJarUrl();

        String getMainClass();

        Collection<JarModuleOutput> getModuleOutputs();
    }

    public static class JarModuleOutput {
        public enum Packaging {
            COPY,
            JAR,
            JAR_AND_LINK
        }

        private final String moduleName;
        private final Packaging packaging;
        private final URI targetUri;

        public JarModuleOutput( final String moduleName, final Packaging packaging, final URI targetUri ) {
            this.moduleName = moduleName;
            this.packaging = packaging;
            this.targetUri = targetUri;
        }

        public Packaging getPackaging() {
            return this.packaging;
        }

        public URI getTargetUri() {
            return this.targetUri;
        }

        public String getModuleName() {
            return this.moduleName;
        }

        @SuppressWarnings( { "RedundantIfStatement" } )
        @Override
        public boolean equals( final Object o ) {
            if ( this == o ) return true;
            if ( o == null || getClass() != o.getClass() ) return false;

            final JarModuleOutput that = (JarModuleOutput) o;

            if ( this.packaging != that.packaging ) return false;
            if ( this.targetUri != null ? !targetUri.equals( that.targetUri ) : that.targetUri != null ) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = this.packaging != null ? packaging.hashCode() : 0;
            result = 31 * result + ( this.targetUri != null ? targetUri.hashCode() : 0 );
            return result;
        }
    }

    public URI getModuleDescriptor() {
        return this.moduleDescriptor;
    }

    public String getName() {
        return this.name;
    }

    public URI getModuleRoot() {
        return UriUtils.getParent( this.moduleDescriptor );
    }

    public String getOutputUrl() {
        return this.outputUrl;
    }

    public String getTestOutputUrl() {
        return this.testOutputUrl;
    }

    public String getContentRootUrl() {
        return this.contentRootUrl;
    }

    public boolean isOutputInherited() {
        return this.inheritOutput;
    }

    public JarSettings getJarSettings() {
        return this.jarSettings;
    }

    public boolean isBuildJar() {
        return this.jarSettings != null;
    }

    public Collection<String> getSourceUrls() {
        return Collections.unmodifiableCollection( this.sourceUrls );
    }

    public Collection<String> getTestSourceUrls() {
        return Collections.unmodifiableCollection( this.testSourceUrls );
    }

    public Collection<Dependency> getDependencies() {
        return Collections.unmodifiableCollection( this.dependencies );
    }

    private Module( final URI moduleDescriptor, final Handler defaultHandler ) throws IllegalArgumentException {
        super( "module", defaultHandler );

        final String fileName = UriUtils.getFilename( moduleDescriptor );
        if ( !fileName.toLowerCase().endsWith( ".iml" ) )
            throw new IllegalArgumentException( "The specified module descriptor \"" + moduleDescriptor +
                    "\" does not point to a valid IDEA module file (.iml)" );

        this.name = fileName.lastIndexOf( '.' ) != -1 ? fileName.substring( 0, fileName.lastIndexOf( '.' ) ) : fileName;
        this.moduleDescriptor = moduleDescriptor;
        this.sourceUrls = new HashSet<String>();
        this.testSourceUrls = new HashSet<String>();
        this.dependencies = new HashSet<Dependency>();

        // Register ignored components
        registerComponentHandler( "FacetManager", ignoreHandler );              // TODO

        // Register handlers
        registerComponentHandler( "NewModuleRootManager", new NewModuleRootManagerHandler() );
        registerComponentHandler( "BuildJarSettings", new BuildJarSettingsHandler() );
    }

    public static Module parse( final URI descriptor ) throws IOException, ParseException, IllegalArgumentException {
        return parse( descriptor, throwHandler );
    }

    public static Module parse( final URI descriptor, final Handler defaultHandler )
            throws IOException, ParseException, IllegalArgumentException {
        if ( descriptor == null )
            throw new IllegalArgumentException( "The module descriptor URI cannot be null." );

        // Instantiate module and load document
        final Module module = new Module( descriptor, defaultHandler );
        final Document document;
        try {
            document = builderFactory.newDocumentBuilder().parse( new File( descriptor ) );
        } catch ( SAXException e ) {
            throw new ParseException( "Cannot parse XML document, see inner exception for details.", e );
        } catch ( ParserConfigurationException e ) {
            throw new ParseException( "XML parser configuration invalid, see inner exception for details.", e );
        }

        // Verify root element
        final String moduleType = extract( document, "/module/@type", "Cannot extract module type" );
        if ( !"JAVA_MODULE".equals( moduleType ) )
            throw new ParseException( "Invalid module type \"" + moduleType + "\", expected \"JAVA_MODULE\"" );

        module.processComponents( document );
        return module;
    }

    @SuppressWarnings( { "RedundantIfStatement" } )
    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        final Module module = (Module) o;

        if ( this.inheritOutput != module.inheritOutput ) return false;
        if ( this.contentRootUrl != null ? !contentRootUrl.equals( module.contentRootUrl )
                : module.contentRootUrl != null )
            return false;
        if ( !setEquals( this.dependencies, module.dependencies ) ) return false;
        if ( this.moduleDescriptor != null ? !moduleDescriptor.equals( module.moduleDescriptor )
                : module.moduleDescriptor != null ) return false;
        if ( this.name != null ? !name.equals( module.name ) : module.name != null ) return false;
        if ( this.outputUrl != null ? !outputUrl.equals( module.outputUrl ) : module.outputUrl != null ) return false;
        if ( !setEquals( this.sourceUrls, module.sourceUrls ) ) return false;
        if ( this.testOutputUrl != null ? !testOutputUrl.equals( module.testOutputUrl ) : module.testOutputUrl != null )
            return false;
        if ( !setEquals( this.testSourceUrls, module.testSourceUrls ) )
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = this.moduleDescriptor != null ? moduleDescriptor.hashCode() : 0;
        result = 31 * result + ( this.outputUrl != null ? outputUrl.hashCode() : 0 );
        result = 31 * result + ( this.testOutputUrl != null ? testOutputUrl.hashCode() : 0 );
        result = 31 * result + ( this.contentRootUrl != null ? contentRootUrl.hashCode() : 0 );
        result = 31 * result + deepHashCode( this.sourceUrls );
        result = 31 * result + deepHashCode( this.testSourceUrls );
        result = 31 * result + deepHashCode( this.dependencies );
        result = 31 * result + ( this.name != null ? name.hashCode() : 0 );
        result = 31 * result + ( this.inheritOutput ? 1 : 0 );
        return result;
    }

    @Override
    protected void generatePropertyMap( final Map<String, String> properties ) {
        properties.put( "MODULE_DIR", getModuleRoot().getPath() );
    }

    @Override
    public String toString() {
        return "IntelliJ IDEA module \"" + getName() + "\"";
    }

    private class NewModuleRootManagerHandler implements IntelliJParserBase.Handler {
        @Override
        public void parse( final String componentName, final Node componentNode )
                throws IllegalArgumentException, ParseException {

            // Parse module descriptor
            Module.this.inheritOutput = "true".equals( extract( componentNode, "@inherit-compiler-output",
                    "Cannot extract output inheritence attribute" ) );
            Module.this.outputUrl = extract( componentNode, "output/@url", "Cannot extract compiler output path" );
            Module.this.testOutputUrl = extract( componentNode, "output-test/@url",
                    "Cannot extract compiler test class output path" );

            // Ensure module output can be resolved
            if ( Module.this.outputUrl == null && !Module.this.inheritOutput )
                throw new ParseException( "Module output not specified and project output inheritence " +
                        "is disabled, cannot resolve module output directory." );

            // Normalize test output
            if ( Module.this.testOutputUrl == null && !Module.this.inheritOutput )
                Module.this.testOutputUrl = Module.this.outputUrl;

            // Parse source folders
            Module.this.contentRootUrl = extract( componentNode, "content/@url", "Cannot extract content root path" );
            for ( final Node sourceFolder : extractAll( componentNode, "content/sourceFolder",
                    "Cannot extract source folders" ) ) {
                final String url = extract( sourceFolder, "@url", "Cannot extract source folder URL" );
                final String testFlag = extract( sourceFolder, "@isTestSource",
                        "Cannot extract isTestSource flag for source folder" );
                final Collection<String> list =
                        "true".equals( testFlag ) ? Module.this.testSourceUrls : Module.this.sourceUrls;
                list.add( url );
            }

            // Parse dependencies
            for ( final Node dependency : extractAll( componentNode, "orderEntry", "Cannot extract dependencies" ) ) {
                final String type = extract( dependency, "@type", "Cannot extract dependency type" );
                if ( "inheritedJdk".equals( type ) ) {
                    // TODO handle JDKs properly
                } else if ( "jdk".equals( type ) ) {
                    // TODO handle JDKs properly
                } else if ( "sourceFolder".equals( type ) ) {
                    // TODO handle forTests
                } else if ( "module-library".equals( type ) ) {
                    for ( final Node libraryNode : extractAll( dependency, "library",
                            "Cannot extract module library descriptor" ) )
                        Module.this.dependencies.add( new ModuleLibraryDependency( new Library( libraryNode ) ) );
                } else if ( "library".equals( type ) ) {
                    final String name = extract( dependency, "@name", "Cannot extract library dependency name" );
                    final LibraryDependency.Level level;
                    try {
                        level = LibraryDependency.Level
                                .parse( extract( dependency, "@level", "Cannot extract library dependency level" ) );
                    } catch ( IllegalArgumentException ignored ) {
                        throw new ParseException(
                                "Cannot parse library dependency level for library \"" + name + "\"" );
                    }

                    if ( level != LibraryDependency.Level.PROJECT )
                        throw new ParseException( "Named " + level + "-level library dependencies are not supported." );

                    Module.this.dependencies.add( new ProjectLibraryDependency( name ) );
                } else if ( "module".equals( type ) ) {
                    Module.this.dependencies.add( new ModuleDependency(
                            extract( dependency, "@module-name",
                                    "Cannot extract module dependency name" ) ) );
                } else throw new ParseException( "Unrecognized order entry type \"" + type + "\"." );
            }
        }
    }

    private class BuildJarSettingsHandler implements Handler {
        @Override
        public void parse( final String componentName, final Node componentNode )
                throws IllegalArgumentException, ParseException {
            if ( !"true".equals( extract( componentNode, "setting[@name='buildJar']/@value",
                    "Cannot extract JAR build setting" ) ) )
                return;

            final String jarUrl = extract( componentNode, "setting[@name='jarUrl']/@value", "Cannot extract JAR URL" );
            if ( jarUrl == null )
                throw new ParseException( "Module JAR build enabled but output URL not specified." );

            final String mainClass = extract( componentNode, "setting[@name='mainClass']/@value",
                    "Cannot extract JAR main class name" );

            final Collection<JarModuleOutput> modules = new HashSet<JarModuleOutput>();
            for ( final Node container : extractAll( componentNode, "containerInfo/containerElement",
                    "Cannot extract JAR container elements" ) ) {
                final String type = extract( container, "@type", "Cannot extract container element type" );
                if ( type == null )
                    throw new ParseException( "JAR container element type not specified." );
                if ( "module".equals( type ) )
                    modules.add( resolveModuleContainerElement( container ) );
                else throw new ParseException( "Unrecognized JAR container element type '" + type + "'" );
            }

            Module.this.jarSettings = new JarSettings() {
                @Override
                public String getJarUrl() {
                    return jarUrl;
                }

                @Override
                public String getMainClass() {
                    return mainClass != null && mainClass.length() == 0 ? null : mainClass;
                }

                @Override
                public Collection<JarModuleOutput> getModuleOutputs() {
                    return Collections.unmodifiableCollection( modules );
                }
            };
        }


        private JarModuleOutput resolveModuleContainerElement( final Node container ) throws ParseException {
            final String moduleName = extract( container, "@name",
                    "Cannot extract module name for JAR module output" );
            if ( moduleName == null )
                throw new ParseException( "Module name not specified for JAR module output." );

            // Extract packaging method
            final String method = extract( container, "attribute[@name='method']/@value",
                    "Cannot extract JAR module output packaging method" );
            if ( method == null )
                throw new ParseException(
                        "Packaging method not specified for JAR module output \"" + moduleName + "\"." );
            final int methodOrdinal;
            try {
                methodOrdinal = Integer.valueOf( method );
            } catch ( NumberFormatException ignored ) {
                throw new ParseException(
                        "Invalid method ordinal \"" + method + "\" specified for JAR module output \"" +
                                moduleName + "\"." );
            }
            if ( methodOrdinal < 1 || methodOrdinal > JarModuleOutput.Packaging.values().length )
                throw new ParseException(
                        "Invalid method ordinal \"" + method + "\" specified for JAR module output \"" +
                                moduleName + "\"." );
            final JarModuleOutput.Packaging packaging = JarModuleOutput.Packaging.values()[ methodOrdinal - 1 ];

            // Extract target URI
            final String uriString = extract( container, "attribute[@name='URI']/@value",
                    "Cannot extract JAR module output target URI" );
            if ( uriString == null )
                throw new ParseException( "Target URI not specified for JAR module output \"" + moduleName + "\"." );
            final URI targetUri;
            try {
                targetUri = new URI( uriString );
            } catch ( URISyntaxException e ) {
                throw new ParseException(
                        "Invalid target URI \"" + uriString + "\" specified for JAR module output \"" + moduleName +
                                "\".", e );
            }

            return new JarModuleOutput( moduleName, packaging, targetUri );
        }
    }
}
