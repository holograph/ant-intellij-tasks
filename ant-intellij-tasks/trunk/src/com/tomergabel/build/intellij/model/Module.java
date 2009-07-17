package com.tomergabel.build.intellij.model;

import com.tomergabel.util.UriUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

public final class Module extends IntelliJParserBase {
    private final URI descriptor;
    private String outputUrl = null;
    private String testOutputUrl = null;
    private String contentRootUrl;
    private final Collection<String> sourceUrls;
    private final Collection<String> testSourceUrls;
    private final Collection<Dependency> depdencies;

    public URI getDescriptor() {
        return this.descriptor;
    }

    public String getName() {
        final String filename = UriUtils.getFilename( this.descriptor );
        return filename.lastIndexOf( '.' ) != -1 ? filename.substring( 0, filename.lastIndexOf( '.' ) ) : filename;
    }

    public URI getModuleRoot() {
        return UriUtils.getParent( this.descriptor );
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

    public Collection<String> getSourceUrls() {
        return Collections.unmodifiableCollection( this.sourceUrls );
    }

    public Collection<String> getTestSourceUrls() {
        return Collections.unmodifiableCollection( this.testSourceUrls );
    }

    public Collection<Dependency> getDependencies() {
        return Collections.unmodifiableCollection( this.depdencies );
    }

    private Module( final URI descriptor, final Handler defaultHandler ) throws IllegalArgumentException {
        super( "module", defaultHandler );

        final String fileName = UriUtils.getFilename( descriptor );
        if ( !fileName.toLowerCase().endsWith( ".iml" ) )
            throw new IllegalArgumentException( "The specified module descriptor \"" + descriptor +
                    "\" does not point to a valid IDEA module file (.iml)" );

        this.descriptor = descriptor;
        this.sourceUrls = new HashSet<String>();
        this.testSourceUrls = new HashSet<String>();
        this.depdencies = new HashSet<Dependency>();
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
        final Element root = document.getDocumentElement();
        final String moduleType = extract( document, "/module/@type", "Cannot extract module type" );
        if ( !"JAVA_MODULE".equals( moduleType ) )
            throw new ParseException( "Invalid module type \"" + moduleType + "\", expected \"JAVA_MODULE\"" );

        // Iterate components
        for ( final Node component : extractAll( root, "component", "Cannot extract module components" ) )
            parseComponent( component, module );

        return module;
    }

    private static void parseComponent( final Node component, final Module module )
            throws ParseException, IllegalArgumentException {
        if ( component == null )
            throw new IllegalArgumentException( "The component node cannot be null." );
        if ( module == null )
            throw new IllegalArgumentException( "The module cannot be null." );

        // Test component name to see if it's supported
        final String componentName = extract( component, "@name", "Cannot extract component name" );
        if ( "NewModuleRootManager".equals( componentName ) ) {

            // Parse module descriptor
            module.outputUrl = extract( component, "output/@url", "Cannot extract compiler output path" );
            module.testOutputUrl = extract( component, "output-test/@url",
                    "Cannot extract compiler test class output path" );

            // Parse source folders
            module.contentRootUrl = extract( component, "content/@url", "Cannot extract content root path" );
            for ( final Node sourceFolder : extractAll( component, "content/sourceFolder",
                    "Cannot extract source folders" ) ) {
                final String url = extract( sourceFolder, "@url", "Cannot extract source folder URL" );
                final String testFlag = extract( sourceFolder, "@isTestSource",
                        "Cannot extract isTestSource flag for source folder" );
                final Collection<String> list = "true".equals( testFlag ) ? module.testSourceUrls : module.sourceUrls;
                list.add( url );
            }

            // Parse dependencies
            for ( final Node dependency : extractAll( component, "orderEntry", "Cannot extract dependencies" ) ) {
                final String type = extract( dependency, "@type", "Cannot extract dependency type" );
                if ( "inheritedJdk".equals( type ) ) {
                    // TODO handle JDKs properly
                } else if ( "sourceFolder".equals( type ) ) {
                    // TODO handle forTests
                } else if ( "library".equals( type ) ) {
                    final String name = extract( dependency, "@name", "Cannot extract library dependency name" );
                    final LibraryDependency.Level level;
                    try {
                        level = LibraryDependency.Level
                                .parse( extract( dependency, "@level", "Cannot extract library dependency level" ) );
                    } catch ( IllegalArgumentException e ) {
                        throw new ParseException(
                                "Cannot parse library dependency level for library \"" + name + "\"" );
                    }
                    module.depdencies.add( new LibraryDependency( level, name ) );
                } else if ( "module".equals( type ) ) {
                    module.depdencies.add( new ModuleDependency(
                            extract( dependency, "@module-name", "Cannot extract module dependency name" ) ) );
                } else throw new ParseException( "Unrecognized order entry type \"" + type + "\"" );
            }
        }
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        final Module module = (Module) o;

        return !( this.contentRootUrl != null ? !contentRootUrl.equals( module.contentRootUrl )
                : module.contentRootUrl != null ) &&
                !( this.depdencies != null ? !depdencies.equals( module.depdencies ) : module.depdencies != null ) &&
                !( this.descriptor != null ? descriptor.compareTo( module.descriptor ) != 0 : module.descriptor != null ) &&
                !( this.outputUrl != null ? !outputUrl.equals( module.outputUrl ) : module.outputUrl != null ) &&
                !( this.sourceUrls != null ? !sourceUrls.equals( module.sourceUrls ) : module.sourceUrls != null ) &&
                !( this.testOutputUrl != null ? !testOutputUrl.equals( module.testOutputUrl )
                        : module.testOutputUrl != null ) &&
                !( this.testSourceUrls != null ? !testSourceUrls.equals( module.testSourceUrls )
                        : module.testSourceUrls != null );

    }

    @Override
    public int hashCode() {
        int result = this.descriptor != null ? descriptor.hashCode() : 0;
        result = 31 * result + ( this.outputUrl != null ? outputUrl.hashCode() : 0 );
        result = 31 * result + ( this.testOutputUrl != null ? testOutputUrl.hashCode() : 0 );
        result = 31 * result + ( this.contentRootUrl != null ? contentRootUrl.hashCode() : 0 );
        result = 31 * result + ( this.sourceUrls != null ? sourceUrls.hashCode() : 0 );
        result = 31 * result + ( this.testSourceUrls != null ? testSourceUrls.hashCode() : 0 );
        result = 31 * result + ( this.depdencies != null ? depdencies.hashCode() : 0 );
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
}
