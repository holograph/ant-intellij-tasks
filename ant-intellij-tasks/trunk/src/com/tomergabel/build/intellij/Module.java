package com.tomergabel.build.intellij;

import com.tomergabel.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

public final class Module {
    private final File root;
    private String outputUrl;
    private String testOutputUrl;
    private String contentRootUrl;
    private Collection<String> sourceUrls;
    private Collection<String> testSourceUrls;
    private Collection<Dependency> depdencies;
    private String name;

    public String getName() {
        return this.name;
    }

    public File getRoot() {
        return this.root;
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
        return this.sourceUrls;
    }

    public Collection<String> getTestSourceUrls() {
        return this.testSourceUrls;
    }

    public Collection<Dependency> getDepdencies() {
        return this.depdencies;
    }

    private Module( File descriptor ) throws IllegalArgumentException {
        final String fileName = descriptor.getName();
        if ( !fileName.toLowerCase().endsWith( ".iml" ) )
            throw new IllegalArgumentException( "The specified module descriptor \"" + descriptor +
                    "\" does not point to a valid IDEA module file (.iml)" );

        this.root = descriptor.getParentFile();
        this.name = fileName.subSequence( 0, fileName.length() - 4 ).toString();   // Strip extension
        this.sourceUrls = new HashSet<String>();
        this.testSourceUrls = new HashSet<String>();
        this.depdencies = new HashSet<Dependency>();
    }

    static final XPath xpath = XPathFactory.newInstance().newXPath();

    private static String extract( Node context, String xpath, String failMessage )
            throws IllegalArgumentException, ParseException {
        if ( context == null )
            throw new IllegalArgumentException( "The context cannot be null." );
        if ( xpath == null )
            throw new IllegalArgumentException( "The XPath expression cannot be null." );
        if ( failMessage == null )
            throw new IllegalArgumentException( "The failure message cannot be null." );

        try {
            return Module.xpath.evaluate( xpath, context );
        } catch ( XPathExpressionException e ) {
            throw new ParseException( failMessage, e );
        }
    }

    private static Collection<Node> extractAll( Node context, String xpath, String failMessage )
            throws IllegalArgumentException, ParseException {
        if ( context == null )
            throw new IllegalArgumentException( "The context cannot be null." );
        if ( xpath == null )
            throw new IllegalArgumentException( "The XPath expression cannot be null." );
        if ( failMessage == null )
            throw new IllegalArgumentException( "The failure message cannot be null." );

        try {
            return XmlUtils.wrapNodeList( (NodeList) Module.xpath.evaluate( xpath, context, XPathConstants.NODESET ) );
        } catch ( XPathExpressionException e ) {
            throw new ParseException( failMessage, e );
        }
    }

    static DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

    static Module parse( File descriptor ) throws IOException, ParseException, IllegalArgumentException {
        if ( descriptor == null )
            throw new IllegalArgumentException( "The module descriptor file path cannot be null." );

        // Instantiate module and load document
        final Module module = new Module( descriptor );
        final Document document;
        try {
            document = builderFactory.newDocumentBuilder().parse( descriptor );
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
}
