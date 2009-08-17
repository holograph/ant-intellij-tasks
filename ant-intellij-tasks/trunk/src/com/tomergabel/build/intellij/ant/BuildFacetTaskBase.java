package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.Facet;
import com.tomergabel.build.intellij.model.ResolutionException;
import static com.tomergabel.util.CollectionUtils.filter;

public abstract class BuildFacetTaskBase<T extends Facet> extends ModuleTaskBase {
    private T facet;

    protected abstract Class<T> getFacetClass();

    protected T resolveFacet() throws ResolutionException {
        if ( this.facet == null ) {
            for ( final T facet : filter( module().getFacets(), getFacetClass() ) )
                if ( this.facet != null )
                    throw new ResolutionException(
                            "Module \"" + module().getName() + "\" declares more than one " +
                                    getFacetClass().getName() +
                                    " instance" );
                else
                    this.facet = facet;

            throw new ResolutionException(
                    "Module \"" + module().getName() + "\" does not specify a " + getFacetClass().getName() +
                            " instance" );
        }
        return this.facet;
    }

    public void setFacet( final T facet ) {
        assertNotExecuted();
        this.facet = facet;
    }
}
