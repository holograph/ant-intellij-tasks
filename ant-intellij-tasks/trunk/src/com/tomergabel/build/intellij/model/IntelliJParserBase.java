package com.tomergabel.build.intellij.model;

import com.tomergabel.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A base class for IntelliJ IDEA metadata file parsers (e.g. module and project files).
 * <p/>
 * Such files typically typically consist of a root node with a number of nested component elements, the name of which
 * determines the particular meaning and schema of the component.
 * <p/>
 * This class provides a common codebase for parsing these components and handling errors. Implementors register
 * component {@link Handler handlers} via {@link #registerComponentHandler(String, Handler)}, typically at construction
 * tieme, and can specify a default handler for unrecognized sections via a constructor argument. A call to {@link
 * #processComponents(org.w3c.dom.Document)} will then process the file, delegating the task of parsing component
 * elements to their respective handlers.
 */
public abstract class IntelliJParserBase {
    /**
     * A static {@link XPath} instnace which can be used to perform XPath queries.
     */
    protected static final XPath xpath = XPathFactory.newInstance().newXPath();
    /**
     * A DOM document builder factory, provided to implementors for convenience.
     */
    protected static DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    /**
     * A {@link Handler} which simply ignores the specified component. Typically used to avoid warnings or throws on
     * unneeded or unsupported components.
     */
    protected static Handler ignoreHandler = new Handler() {
        @Override
        public void parse( final String componentName, final Node componentNode )
                throws IllegalArgumentException, ParseException {
        }
    };
    /**
     * A {@link Handler} which throws an exception upon encountering a component. This is typically used as the default
     * handler.
     */
    public static Handler throwHandler = new Handler() {
        @Override
        public void parse( final String componentName, final Node componentNode )
                throws IllegalArgumentException, ParseException {
            if ( componentNode == null )
                throw new IllegalArgumentException( "The component node cannot be null." );
            throw new ParseException( "Unknown component \"" + componentName + "\"" );
        }
    };

    /**
     * Provides the public interface for a component handler.
     * <p/>
     * A component handler is registered using a specific component name, and the parser delegates parsing of each
     * component to the handler corresponding to its name.
     */
    public interface Handler {
        /**
         * Parses the specified component.
         *
         * @param componentName The component name.
         * @param componentNode The DOM node containing the component configuration.
         * @throws IllegalArgumentException <ul><li>The component name cannot be null.</li><li>The component node cannot
         *                                  be null.</li></ul>
         * @throws ParseException           An error has occurred while parsing the component.
         */
        void parse( String componentName, Node componentNode ) throws IllegalArgumentException, ParseException;
    }

    /**
     * Creates and returns a new instance of {@link com.tomergabel.build.intellij.model.IntelliJParserBase}.
     *
     * @param rootNodeName   The name of the root node (e.g. "project" for project files).
     * @param defaultHandler The default component handler. Components without a registered handler are delegated to the
     *                       default handler for parsing.
     * @throws IllegalArgumentException The default handler cannot be null.
     */
    public IntelliJParserBase( final String rootNodeName, final Handler defaultHandler )
            throws IllegalArgumentException {
        if ( defaultHandler == null )
            throw new IllegalArgumentException( "The default handler cannot be null." );

        this.rootNodeName = rootNodeName;
        this.defaultHandler = defaultHandler;
    }

    /**
     * The system line separator.
     */
    protected static final String LINE_SEPERATOR = System.getProperty( "line.separator" );

    /**
     * A utility function which takes an XPath expression, evaluates it to an XML node and returns its value.
     *
     * @param context     The context node on which to evaluate the XPath expression.
     * @param xpath       The XPath expression to evaluate.
     * @param failMessage The error message should evaluation failed.
     * @return The value of the node the XPath expression evaulated to, or {@literal null} if no node matched the
     *         expression.
     * @throws IllegalArgumentException <ul><li>The context node cannot be null.</li><li>The XPath expression cannot be
     *                                  null.</li><li>The failure message cannot be null.</li></ul>
     * @throws ParseException           XPath evaluation failed.
     */
    protected static String extract( final Node context, final String xpath, final String failMessage )
            throws IllegalArgumentException, ParseException {
        if ( context == null )
            throw new IllegalArgumentException( "The context node cannot be null." );
        if ( xpath == null )
            throw new IllegalArgumentException( "The XPath expression cannot be null." );
        if ( failMessage == null )
            throw new IllegalArgumentException( "The failure message cannot be null." );

        try {
            final NodeList list = (NodeList) Module.xpath.evaluate( xpath, context, XPathConstants.NODESET );
            assert list != null;
            if ( list.getLength() > 1 ) throw new ParseException(
                    failMessage + LINE_SEPERATOR + "More than one node matches expression '" + xpath + "'" );
            return list.getLength() == 0 ? null : list.item( 0 ).getTextContent();     // Assumes no child nodes
        } catch ( XPathExpressionException e ) {
            throw new ParseException( failMessage, e );
        }
    }

    /**
     * A utility function which takes an XPath expression, evaulates it and returns all matching nodes.
     *
     * @param context     The context node on which to evaluate the XPath expression.
     * @param xpath       The XPath expression to evaluate.
     * @param failMessage The error message should evaluation failed.
     * @return The collection of nodes matching the expression.
     * @throws IllegalArgumentException <ul><li>The context node cannot be null.</li><li>The XPath expression cannot be
     *                                  null.</li><li>The failure message cannot be null.</li></ul>
     * @throws ParseException           XPath evaluation failed.
     */
    protected static Collection<Node> extractAll( final Node context, final String xpath, final String failMessage )
            throws IllegalArgumentException, ParseException {
        if ( context == null )
            throw new IllegalArgumentException( "The context node cannot be null." );
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

    /**
     * Maps property names to their respective values.
     */
    private Map<String, String> propertyCache;
    /**
     * Maps component names to their respective handlers.
     */
    private final Map<String, Handler> handlerMap = new HashMap<String, Handler>();
    /**
     * The default handler.
     */
    private final Handler defaultHandler;
    /**
     * The root node name (e.g. "project" for project files).
     */
    private final String rootNodeName;

    /**
     * Retreives all exported properties for this IDEA metadata file.
     * <p/>
     * <em>Note to implementors:</em> {@link #generatePropertyMap(java.util.Map)} is called once to generate the map,
     * which is then cached.
     *
     * @return A map of property names to their respective values.
     */
    public final Map<String, String> getProperties() {
        if ( this.propertyCache == null ) {
            final HashMap<String, String> map = new HashMap<String, String>();
            generatePropertyMap( map );
            this.propertyCache = Collections.unmodifiableMap( map );
        }
        return this.propertyCache;
    }

    /**
     * Generates the property map for this IDEA metadata file.
     * <p/>
     * <em>Note to implementors:</em> This method will be called once by {@link #getProperties()}, and the values placed
     * in the map are cached by the base implementation. Further calls to {@link #getProperties()} are satisfied from
     * cache. It is therefore recommended to ensure the data you return cannot become stale.
     *
     * @param properties The property map in which this IDEA metadata file's properties are placed.
     * @throws IllegalArgumentException The property map cannot be null.
     */
    protected abstract void generatePropertyMap( Map<String, String> properties ) throws IllegalArgumentException;

    /**
     * Registers a component handler for a specific component name.
     * <p/>
     * Registering a handler for a previously-registered component name will override the previous registration.
     *
     * @param componentName The component name.
     * @param handler       The component handler.
     * @throws IllegalArgumentException <ul><li>The component name cannot be null.</li><li>The component handler cannot
     *                                  be null.</li></ul>
     */
    protected final void registerComponentHandler( final String componentName, final Handler handler )
            throws IllegalArgumentException {
        if ( componentName == null )
            throw new IllegalArgumentException( "The component name cannot be null." );
        if ( handler == null )
            throw new IllegalArgumentException( "The component handler cannot be null." );

        this.handlerMap.put( componentName, handler );
    }

    /**
     * Processes all components in this IDEA metadata file.
     * <p/>
     * <em>Note to implementors:</em> The
     *
     * @param document The DOM document parsed from the IDEA metadata file.
     * @throws ParseException           An error has occurred during parsing.
     * @throws IllegalArgumentException The document cannot be null.
     */
    protected final void processComponents( final Document document ) throws ParseException {
        if ( document == null )
            throw new IllegalArgumentException( "The document cannot be null." );

        for ( final Node component : extractAll( document, this.rootNodeName + "/component",
                "Cannot extract project components" ) ) {
            final String componentName = extract( component, "@name", "Cannot extract component name" );

            // Resolve handler
            final Handler handler = this.handlerMap.containsKey( componentName ) ? this.handlerMap.get( componentName )
                    : this.defaultHandler;

            // Parse component
            handler.parse( componentName, component );
        }
    }
}
