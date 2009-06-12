package com.tomergabel.build.intellij.model;

import com.tomergabel.util.XmlUtils;
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

    public final Map<String, String> getProperties() {
        if ( this.propertyCache == null ) {
            HashMap<String, String> map = new HashMap<String, String>();
            generatePropertyMap( map );
            this.propertyCache = Collections.unmodifiableMap( map );
        }
        return this.propertyCache;
    }

    protected abstract void generatePropertyMap( Map<String, String> properties );
}
