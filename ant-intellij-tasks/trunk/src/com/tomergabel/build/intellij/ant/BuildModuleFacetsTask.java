package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.EjbFacet;
import com.tomergabel.build.intellij.model.Facet;
import com.tomergabel.build.intellij.model.WebFacet;
import org.apache.tools.ant.BuildException;

public class BuildModuleFacetsTask extends ModuleTaskBase {
    @Override
    protected void executeTask() throws BuildException {
        for ( final Facet facet : module().getFacets() ) {
            if ( facet instanceof EjbFacet ) {
            } else if ( facet instanceof WebFacet ) {

            }
        }
    }
}
