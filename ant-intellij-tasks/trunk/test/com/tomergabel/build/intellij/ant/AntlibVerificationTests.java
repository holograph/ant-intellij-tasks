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

package com.tomergabel.build.intellij.ant;

import com.tomergabel.util.PathUtils;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
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
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Enforces the following constraints on tasks, conditions and the antlib: <ul><li>All classes defining tasks must end
 * with 'Task'</li> <li>Likewise, condition classes must end with 'Condition'</li> <li>All tasks and conditions meeting
 * the above criteria must be represented in the antlib</li> <li>The antlib must not contain task or type definitions
 * that cannot be resolved to a runtime class using test runner classpath (this includes all ant-intellij-task
 * sources.)</li> </ul>
 */
public class AntlibVerificationTests {
    static final XPath xpath = XPathFactory.newInstance().newXPath();
    static Document antlib;

    @BeforeClass
    public static void setup()
            throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        antlib = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse( AntUtils.class.getResourceAsStream( "antlib.xml" ) );
    }

    @Test
    public void ensureAllTasksAreRepresented() throws XPathExpressionException, URISyntaxException {
        assertClassesAreRepresnted( findPairs( "taskdef" ), "Task.class" );
    }

    @Test
    public void ensureRepresentedTaskNameConvention() throws XPathExpressionException {
        final StringBuilder failed = new StringBuilder();
        for ( final Map.Entry<String, String> pair : findPairs( "taskdef" ).entrySet() )
            if ( !pair.getKey().endsWith( "Task" ) )
                failed.append( "Incorrectly declared task " ).append( pair.getValue() )
                        .append( ": class name does not end with 'Task'." );
        if ( failed.length() > 0 )
            fail( failed.toString() );
    }

    @Test
    public void ensureAllRepresentedTasksAreValid() throws XPathExpressionException {
        assertClassesExist( findPairs( "taskdef" ) );
    }

    @Test
    public void ensureAllConditionsAreRepresented() throws URISyntaxException, XPathExpressionException {
        assertClassesAreRepresnted( findPairs( "typedef" ), "Condition.class" );
    }

    @Test
    public void ensureRepresentedConditionNameConvention() throws XPathExpressionException {
        final StringBuilder failed = new StringBuilder();
        for ( final Map.Entry<String, String> pair : findPairs( "typedef" ).entrySet() )
            if ( !pair.getKey().endsWith( "Condition" ) )
                failed.append( "Incorrectly declared condition " ).append( pair.getValue() )
                        .append( ": class name does not end with 'Condition'." );
        if ( failed.length() > 0 )
            fail( failed.toString() );
    }


    @Test
    public void ensureAllRepresentedConditionsAreValid() throws XPathExpressionException {
        assertClassesExist( findPairs( "typedef" ) );
    }

    Set<File> recursiveFindWithSuffix( final File root, final String suffix ) {
        final Set<File> files = new HashSet<File>();

        // Iterate directories
        for ( final File subroot : root.listFiles( new FileFilter() {
            @Override
            public boolean accept( final File pathname ) {
                return pathname.isDirectory();
            }
        } ) )
            files.addAll( recursiveFindWithSuffix( subroot, suffix ) );

        // Iterate files
        files.addAll( Arrays.asList( root.listFiles( new FileFilter() {
            @Override
            public boolean accept( final File pathname ) {
                return !pathname.getPath().contains( "$" ) &&   // Ignore inner classes
                        pathname.getPath().endsWith( suffix );
            }
        } ) ) );

        return files;
    }

    private void assertClassesAreRepresnted( final Map<String, String> pairs, final String suffix )
            throws URISyntaxException {
        assert pairs != null;
        assert suffix.endsWith( ".class" );

        final File root = new File( AntUtils.class.getResource( "." ).toURI() );

        File srcroot = root;
        int count = AntUtils.class.getPackage().getName().split( "\\." ).length;
        while ( count-- > 0 )
            srcroot = srcroot.getParentFile();

        final List<String> failed = new ArrayList<String>();
        for ( final File file : recursiveFindWithSuffix( root, suffix ) ) {
            final String relative = PathUtils.relativize( srcroot, file );
            final String classname = relative.substring( 0, relative.length() - 6 ).replace( File.separatorChar, '.' );
            System.out.println( "Verifying " + classname + "..." );
            if ( !pairs.containsKey( classname ) )
                failed.add( classname );
        }

        if ( failed.size() > 0 )
            fail( "ant-intellij-tasks antlib misses declarations for the following classes: " + failed );
    }

    private Map<String, String> findPairs( final String def ) throws XPathExpressionException {
        final NodeList list = (NodeList) xpath.evaluate( def, antlib.getDocumentElement(), XPathConstants.NODESET );
        final Map<String, String> pairs = new HashMap<String, String>( list.getLength() );
        for ( int i = 0; i < list.getLength(); ++i ) {
            final Node node = list.item( i );
            pairs.put( node.getAttributes().getNamedItem( "classname" ).getNodeValue(),
                    node.getAttributes().getNamedItem( "name" ).getNodeValue() );
        }
        return pairs;
    }

    private void assertClassesExist( final Map<String, String> pairs ) throws XPathExpressionException {
        final Set<String> failed = new HashSet<String>();
        for ( final Map.Entry<String, String> pair : pairs.entrySet() )
            try {
                System.out.println( "Verifying " + pair.getValue() + "=>" + pair.getKey() );
                Class.forName( pair.getKey() );
            } catch ( ClassNotFoundException ignored ) {
                failed.add( pair.getValue() );
            }
        if ( failed.size() > 0 )
            fail( "ant-intellij-tasks antlib defines the following nonexistant tasks: " + failed );
    }
}
