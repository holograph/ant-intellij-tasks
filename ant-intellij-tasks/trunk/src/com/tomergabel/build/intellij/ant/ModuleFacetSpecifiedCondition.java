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

import com.tomergabel.build.intellij.model.Facet;
import static com.tomergabel.util.CollectionUtils.filter;
import org.apache.tools.ant.BuildException;

public class ModuleFacetSpecifiedCondition extends ModuleConditionBase {
    protected String facetName;

    public String getFacet() {
        return this.facetName;
    }

    public void setFacet( final String facetName ) {
        this.facetName = facetName;
    }

    @Override
    protected boolean evaluate() throws BuildException {
        if ( this.facetName == null )
            throw new BuildException( "Facet type (attribute 'facet') was not specified." );

        final Class<? extends Facet> facet = Facet.resolveFacetClass( this.facetName );
        if ( facet == null )
            throw new BuildException( "Unknown facet type \"" + this.facetName + "\"" );

        return filter( module().getFacets(), facet ).iterator().hasNext();
    }
}
