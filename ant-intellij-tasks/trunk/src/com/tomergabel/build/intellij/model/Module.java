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
    private String outputUrl;
    private String testOutputUrl;
    private String contentRootUrl;
    private Collection<String> sourceUrls;
    private Collection<String> testSourceUrls;
    private Collection<Dependency> depdencies;

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

    public Collection<Dependency> getDepdencies() {
        return Collections.unmodifiableCollection( this.depdencies );
    }

    private Module( URI descriptor, Handler defaultHandler ) throws IllegalArgumentException {
        super( defaultHandler );

        final String fileName = UriUtils.getFilename( descriptor );
        if ( !fileName.toLowerCase().endsWith( ".iml" ) )
            throw new IllegalArgumentException( "The specified module descriptor \"" + descriptor +
                    "\" does not point to a valid IDEA module file (.iml)" );

        this.descriptor = descriptor;
        this.sourceUrls = new HashSet<String>();
        this.testSourceUrls = new HashSet<String>();
        this.depdencies = new HashSet<Dependency>();
    }

    public static Module parse( URI descriptor ) throws IOException, ParseException, IllegalArgumentException {
        return parse( descriptor, throwHandler );
    }

    public static Module parse( URI descriptor, Handler defaultHandler )
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
        if ( !moduleType.equals( "JAVA_MODULE" ) )
            throw new ParseException( "Invalid module type \"" + moduleType + "\", expected \"JAVA_MODULE\"" );

        // Iterate components
        for ( Node component : extractAll( root, "component", "Cannot extract module components" ) )
            parseComponent( component, module );

        return module;
    }

    private static void parseComponent( Node component, Module module )
            throws ParseException, IllegalArgumentException {
        if ( component == null )
            throw new IllegalArgumentException( "The component node cannot be null." );
        if ( module == null )
            throw new IllegalArgumentException( "The module cannot be null." );

        // Test component name to see if it's supported
        final String componentName = extract( component, "@name", "Cannot extract component name" );
        if ( componentName.equals( "NewModuleRootManager" ) ) {

            // Parse module descriptor
            module.outputUrl = extract( component, "output/@url", "Cannot extract compiler output path" );
            module.testOutputUrl = extract( component, "output-test/@url",
                    "Cannot extract compiler test class output path" );

            // Parse source folders
            module.contentRootUrl = extract( component, "content/@url", "Cannot extract content root path" );
            for ( Node sourceFolder : extractAll( component, "content/sourceFolder",
                    "Cannot extract source folders" ) ) {
                final String url = extract( sourceFolder, "@url", "Cannot extract source folder URL" );
                final String testFlag = extract( sourceFolder, "@isTestSource",
                        "Cannot extract isTestSource flag for source folder" );
                final Collection<String> list = testFlag.equals( "true" ) ? module.testSourceUrls : module.sourceUrls;
                list.add( url );
            }

            // Parse dependencies
            for ( Node dependency : extractAll( component, "orderEntry", "Cannot extract dependencies" ) ) {
                final String type = extract( dependency, "@type", "Cannot extract dependency type" );
                if ( type.equals( "inheritedJdk" ) ) {
                    // TODO handle JDKs properly
                } else if ( type.equals( "sourceFolder" ) ) {
                    // TODO handle forTests
                } else if ( type.equals( "library" ) ) {
                    final String name = extract( dependency, "@name", "Cannot extract library dependency name" );
                    final LibraryDependency.Scope scope;
                    try {
                        scope = LibraryDependency.Scope
                                .parse( extract( dependency, "@level", "Cannot extract library dependency scope" ) );
                    } catch ( IllegalArgumentException e ) {
                        throw new ParseException(
                                "Cannot parse library dependency scope for library \"" + name + "\"" );
                    }
                    module.depdencies.add( new LibraryDependency( scope, name ) );
                } else if ( type.equals( "module" ) ) {
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

        return !( contentRootUrl != null ? !contentRootUrl.equals( module.contentRootUrl )
                : module.contentRootUrl != null ) &&
                !( depdencies != null ? !depdencies.equals( module.depdencies ) : module.depdencies != null ) &&
                !( descriptor != null ? descriptor.compareTo( module.descriptor ) != 0 : module.descriptor != null ) &&
                !( outputUrl != null ? !outputUrl.equals( module.outputUrl ) : module.outputUrl != null ) &&
                !( sourceUrls != null ? !sourceUrls.equals( module.sourceUrls ) : module.sourceUrls != null ) &&
                !( testOutputUrl != null ? !testOutputUrl.equals( module.testOutputUrl )
                        : module.testOutputUrl != null ) &&
                !( testSourceUrls != null ? !testSourceUrls.equals( module.testSourceUrls )
                        : module.testSourceUrls != null );

    }

    @Override
    public int hashCode() {
        int result = descriptor != null ? descriptor.hashCode() : 0;
        result = 31 * result + ( outputUrl != null ? outputUrl.hashCode() : 0 );
        result = 31 * result + ( testOutputUrl != null ? testOutputUrl.hashCode() : 0 );
        result = 31 * result + ( contentRootUrl != null ? contentRootUrl.hashCode() : 0 );
        result = 31 * result + ( sourceUrls != null ? sourceUrls.hashCode() : 0 );
        result = 31 * result + ( testSourceUrls != null ? testSourceUrls.hashCode() : 0 );
        result = 31 * result + ( depdencies != null ? depdencies.hashCode() : 0 );
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
