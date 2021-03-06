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
