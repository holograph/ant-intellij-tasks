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
