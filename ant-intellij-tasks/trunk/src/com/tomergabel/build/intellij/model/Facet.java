package com.tomergabel.build.intellij.model;

import org.w3c.dom.Node;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public abstract class Facet extends ParserBase {
    public Facet( final Node facetNode ) throws IllegalArgumentException, ParseException {
        super();
        if ( facetNode == null )
            throw new IllegalArgumentException( "The facet node cannot be null." );
    }

    private static final Map<String, Constructor<? extends Facet>> facetTypeMap = new HashMap<String, Constructor<? extends Facet>>();

    static {
        try {
            facetTypeMap.put( "web", WebFacet.class.getConstructor( Node.class ) );
            facetTypeMap.put( "ejb", EjbFacet.class.getConstructor( Node.class ) );
        } catch ( NoSuchMethodException e ) {
            // Safety net, should never happen
            // TODO move parsing away from constructor. Much cleaner that way.
            throw new RuntimeException( "Cannot resolve facet class constructor.", e );
        }
    }

    public static Class<? extends Facet> resolveFacetClass( final String typeName ) {
        if ( typeName == null )
            throw new IllegalArgumentException( "The facet type name cannot be null." );

        final Constructor<? extends Facet> ctor = facetTypeMap.get( typeName.toLowerCase() );
        return ctor != null ? ctor.getDeclaringClass() : null;
    }

    public static Facet create( final String typeName, final Node facetNode ) throws ParseException {
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
