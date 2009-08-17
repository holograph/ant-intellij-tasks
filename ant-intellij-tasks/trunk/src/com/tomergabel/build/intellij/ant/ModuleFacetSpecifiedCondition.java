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
