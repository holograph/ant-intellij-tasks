package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.PackageFacetBase;
import com.tomergabel.build.intellij.model.ResolutionException;
import com.tomergabel.build.intellij.model.WebFacet;
import com.tomergabel.util.UriUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ResourceCollection;

public class BuildWebFacetTask extends BuildPackageFacetTaskBase<WebFacet> {
    @Override
    protected Class<WebFacet> getFacetClass() {
        return WebFacet.class;
    }

    @Override
    protected void executeTask() throws BuildException {
        final WebFacet facet;
        try {
            facet = resolveFacet();
        } catch ( ResolutionException e ) {
            throw new BuildException( e );
        }

        final ResourceCollection rc;
        try {
            rc = resolveWarResources( facet );
        } catch ( ResolutionException e ) {
            throw new BuildException( e );
        }

        if ( facet.isTargetEnabled() ) {
            final War war = createWarTask();
            try {
                war.setDestFile( UriUtils.getFile( resolver().resolveUriString( facet.getTargetUrl() ) ) );
            } catch ( ResolutionException e ) {
                throw new BuildException( "Cannot resolve WAR output URL.", e );
            }
            try {
                war.setWebxml( UriUtils.getFile( resolver().resolveUriString( facet.getWebDescriptorUrl() ) ) );
            } catch ( ResolutionException e ) {
                throw new BuildException( "Cannot resolve web descriptor file (web.xml) URL.", e );
            }
            war.add( rc );
        } else if ( facet.isExplodeEnabled() ) {
        }

    }

    protected ResourceCollection resolveWarResources( final WebFacet facet ) throws ResolutionException {
        if ( facet == null )
            throw new IllegalArgumentException( "The web facet cannot be null." );

        final Path path = new Path( getProject() );
        for ( final PackageFacetBase.Root source : facet.getSourceRoots() )
            path.add( resolveCompileOutput( resolver(), source ) );
        path.add( resolvePackagingElements() );
        // TODO web roots

        // TODO web descriptor
        return path;
    }

    protected War createWarTask() {
        return (War) getProject().createTask( "war" );
    }
}
