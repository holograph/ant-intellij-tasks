/*
	Copyright 2009 Tomer Gabel <tomer@tomergabel.com>

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


	ant-intellij-tasks project (http://code.google.com/p/ant-intellij-tasks/)

	$Id$
*/

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
     * @throws ParseException           <ul><li>XPath evaluation failed.</li><li>More than one node matches the
     *                                  specified expression.</li></ul>
     */
    static Node extractNode( final Node context, final String xpath, final String failMessage )
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
            return list.getLength() == 0 ? null : list.item( 0 );
        } catch ( XPathExpressionException e ) {
            throw new ParseException( failMessage, e );
        }
    }

    /**
     * A utility function which takes an XPath expression, evaluates it and returns the corresponding XML node.
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
        final Node node = extractNode( context, xpath, failMessage );
        return node == null ? null : node.getTextContent();     // Assumes no child nodes
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
