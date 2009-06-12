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

public abstract class IntelliJParserBase {

    protected static final XPath xpath = XPathFactory.newInstance().newXPath();
    protected static DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    protected static Handler ignoreHandler = new Handler() {
        @Override
        public void parse( String componentName, Node componentNode ) throws IllegalArgumentException, ParseException {
        }
    };
    public static Handler throwHandler = new Handler() {
        @Override
        public void parse( String componentName, Node componentNode ) throws IllegalArgumentException, ParseException {
            if ( componentNode == null )
                throw new IllegalArgumentException( "The component node cannot be null." );
            throw new ParseException( "Unknown component \"" + componentName + "\"" );
        }
    };

    public interface Handler {
        void parse( String componentName, Node componentNode ) throws IllegalArgumentException, ParseException;
    }

    public IntelliJParserBase( final Handler defaultHandler ) {
        this.defaultHandler = defaultHandler;
    }

    protected static String extract( Node context, String xpath, String failMessage )
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

    protected static Collection<Node> extractAll( Node context, String xpath, String failMessage )
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

    private Map<String, String> propertyCache;
    private Map<String, Handler> handlerMap = new HashMap<String, Handler>();
    private final Handler defaultHandler;

    public final Map<String, String> getProperties() {
        if ( this.propertyCache == null ) {
            HashMap<String, String> map = new HashMap<String, String>();
            generatePropertyMap( map );
            this.propertyCache = Collections.unmodifiableMap( map );
        }
        return this.propertyCache;
    }

    protected abstract void generatePropertyMap( Map<String, String> properties );

    protected final void registerComponentHandler( String componentName, Handler handler )
            throws IllegalArgumentException {
        if ( componentName == null )
            throw new IllegalArgumentException( "Component name cannot be null." );
        if ( handler == null )
            throw new IllegalArgumentException( "Component handler cannot be null." );

        handlerMap.put( componentName, handler );
    }

    protected final void processComponents( final Document document ) throws ParseException {
        for ( Node component : extractAll( document, "project/component", "Cannot extract project components" ) ) {
            final String componentName = extract( component, "@name", "Cannot extract component name" );

            // Resolve handler
            final Handler handler = this.handlerMap.containsKey( componentName ) ? this.handlerMap.get( componentName )
                    : this.defaultHandler;

            // Parse component
            handler.parse( componentName, component );
        }
    }
}
