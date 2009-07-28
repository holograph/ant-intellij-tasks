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

class ParserBase {
    /**
     * A static {@link javax.xml.xpath.XPath} instnace which can be used to perform XPath queries.
     */
    static final XPath xpath = XPathFactory.newInstance().newXPath();
    /**
     * A DOM document builder factory, provided to implementors for convenience.
     */
    static DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    /**
     * The system line separator.
     */
    static final String LINE_SEPERATOR = System.getProperty( "line.separator" );

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
    static String extract( final Node context, final String xpath, final String failMessage )
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
    static Collection<Node> extractAll( final Node context, final String xpath, final String failMessage )
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

}
