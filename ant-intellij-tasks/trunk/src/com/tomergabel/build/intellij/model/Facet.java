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

import org.w3c.dom.Node;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * A base parser class for all module facets.
 */
public abstract class Facet extends ParserBase {
    /**
     * Creats and returns a new instance of {@link Facet}.
     *
     * @param facetNode The XML node containing the facet description.
     * @throws IllegalArgumentException The facet node cannot be null.
     * @throws ParseException           An error has occurred while parsing the facet node.
     */
    public Facet( final Node facetNode ) throws IllegalArgumentException, ParseException {
        super();
        if ( facetNode == null )
            throw new IllegalArgumentException( "The facet node cannot be null." );
    }

    /**
     * Statically maps a facet type key to the appropriate constructor.
     */
    private static final Map<String, Constructor<? extends Facet>> facetTypeMap;

    /** Static c'tor */
    static {
        // Build the facet map
        try {
            facetTypeMap = new HashMap<String, Constructor<? extends Facet>>();
            facetTypeMap.put( "web", WebFacet.class.getConstructor( Node.class ) );
            facetTypeMap.put( "ejb", EjbFacet.class.getConstructor( Node.class ) );
        } catch ( NoSuchMethodException e ) {
            // Safety net, should never happen
            // TODO move parsing away from constructor. Much cleaner that way.
            throw new RuntimeException( "Cannot resolve facet class constructor.", e );
        }
    }

    /**
     * Resolves a specified facet key to its corresponding class.
     *
     * @param typeName The facet type name (as appears in the module descriptor).
     * @return The facet class which matches the specified facet type, or {@literal null} if unavailable.
     */
    public static Class<? extends Facet> resolveFacetClass( final String typeName ) {
        if ( typeName == null )
            throw new IllegalArgumentException( "The facet type name cannot be null." );

        final Constructor<? extends Facet> ctor = facetTypeMap.get( typeName.toLowerCase() );
        return ctor != null ? ctor.getDeclaringClass() : null;
    }

    /**
     * Creates a facet instance for the specified node.
     *
     * @param typeName  The type key from the facet descriptor.
     * @param facetNode The XML node containing the facet descriptor.
     * @return A {@link Facet} instance, or {@literal null} if the facet type key is not recognized.
     * @throws ParseException           An error has occurred while parsing the facet descriptor.
     * @throws IllegalArgumentException <ul><li>The facet type name cannot be null.</li><li>The facet node cannot be
     *                                  null.</li></ul>
     */
    public static Facet create( final String typeName, final Node facetNode )
            throws IllegalArgumentException, ParseException {
        if ( typeName == null )
            throw new IllegalArgumentException( "The facet type name cannot be null." );
        if ( facetNode == null )
            throw new IllegalArgumentException( "The facet node cannot be null." );

        final Constructor<? extends Facet> ctor = facetTypeMap.get( typeName.toLowerCase() );
        if ( ctor == null )
            return null;

        try {
            return ctor.newInstance( facetNode );
        } catch ( InstantiationException e ) {
            // Safety net, should never happen
            throw new IllegalStateException( e );
        } catch ( IllegalAccessException e ) {
            // Safety net, should never happen
            throw new IllegalStateException( e );
        } catch ( InvocationTargetException e ) {
            if ( e.getCause() instanceof ParseException )
                throw (ParseException) e.getCause();
            else
                throw new IllegalStateException( "Unexpected exception thrown from facet constructor.", e );
        }
    }
}
