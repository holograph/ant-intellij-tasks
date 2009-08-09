package com.tomergabel.build.intellij.model;

import org.w3c.dom.Node;

import java.util.Collection;

public class EjbFacet extends PackageFacetBase {
    private final Collection<Root> ejbRoots;

    public Collection<Root> getEjbRoots() {
        return this.ejbRoots;
    }

    public EjbFacet( final Node facetNode ) throws IllegalArgumentException, ParseException {
        super( facetNode );

        // Parse roots
        this.ejbRoots = parseRoots( facetNode, "ejbRoots" );
    }
}
