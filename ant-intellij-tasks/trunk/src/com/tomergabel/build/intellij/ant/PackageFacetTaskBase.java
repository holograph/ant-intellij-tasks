package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.PackageFacetBase;
import com.tomergabel.build.intellij.model.PackagingContainer;
import com.tomergabel.build.intellij.model.ResolutionException;
import static com.tomergabel.util.CollectionUtils.filter;

import java.util.Iterator;

public abstract class PackageFacetTaskBase<T extends PackageFacetBase> extends PackageTaskBase {
    private T facet;

    protected abstract Class<T> getFacetClass();

    protected T resolveFacet() throws ResolutionException {
        if ( this.facet == null ) {
            final Iterator<T> facets = filter( module().getFacets(), getFacetClass() ).iterator();
            if ( !facets.hasNext() )
                throw new ResolutionException(
                        "Module \"" + module().getName() + "\" does not specify a " + getFacetClass().getSimpleName() +
                                " instance" );

            this.facet = facets.next();
            if ( facets.hasNext() )
                throw new ResolutionException(
                        "Module \"" + module().getName() + "\" specifies more than one " +
                                getFacetClass().getSimpleName() +
                                " instance" );
        }
        return this.facet;
    }

    public void setFacet( final T facet ) {
        assertNotExecuted();
        this.facet = facet;
    }

    @Override
    protected PackagingContainer resolvePackagingContainer() throws ResolutionException {
        return resolveFacet().getPackagingContainer();
    }
}
