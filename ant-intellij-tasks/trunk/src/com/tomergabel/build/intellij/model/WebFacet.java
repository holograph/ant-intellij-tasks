package com.tomergabel.build.intellij.model;

import org.w3c.dom.Node;

import java.util.Collection;

public class WebFacet extends PackageFacetBase {
    private final String webDescriptorUrl;
    private final Collection<Root> webRoots;
    private final Collection<Root> sourceRoots;

    public String getWebDescriptorUrl() {
        return this.webDescriptorUrl;
    }

    public Collection<Root> getWebRoots() {
        return this.webRoots;
    }

    public Collection<Root> getSourceRoots() {
        return this.sourceRoots;
    }

    public WebFacet( final Node facetNode ) throws IllegalArgumentException, ParseException {
        super( facetNode );

        // Extract web descriptor URL
        this.webDescriptorUrl = extract( facetNode, "configuration/descriptors/deploymentDescriptor/@url",
                "Cannot extract web descriptor URL." );
        if ( this.webDescriptorUrl == null )
            throw new ParseException( "Module specifies a WAR facet with no web descriptor." );

        // Parse roots
        this.webRoots = parseRoots( facetNode, "webroots" );
        this.sourceRoots = parseRoots( facetNode, "sourceRoots" );
    }

    @Override
    protected String getShortName() {
        return "Web facet";
    }
}
