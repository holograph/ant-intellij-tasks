package com.tomergabel.build.intellij.model;

import org.w3c.dom.Node;

public abstract class Facet extends ParserBase {
    public Facet( final Node facetNode ) throws IllegalArgumentException, ParseException {
        super();
        if ( facetNode == null )
            throw new IllegalArgumentException( "The facet node cannot be null." );
    }
}
