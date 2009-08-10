package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.EjbFacet;
import com.tomergabel.build.intellij.model.PackageFacetBase;
import com.tomergabel.build.intellij.model.ResolutionException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ZipFileSet;

public class BuildEjbFacetTask extends BuildFacetTaskBase<EjbFacet> {
    @Override
    protected void executeTask() throws BuildException {
        final Jar jar = buildJarTask();
    }

    private Jar buildJarTask() {
        final EjbFacet facet;
        try {
            facet = resolveFacet();
        } catch ( ResolutionException e ) {
            throw new BuildException( e );
        }

        final Jar jar = createJarTask();
        //jar.setDestFile( facet.getTargetUrl() );
    }

    protected Jar createJarTask() {
        return (Jar) getProject().createTask( "jar" );
    }

    @Override
    protected Class<EjbFacet> getFacetClass() {
        return EjbFacet.class;
    }
}
