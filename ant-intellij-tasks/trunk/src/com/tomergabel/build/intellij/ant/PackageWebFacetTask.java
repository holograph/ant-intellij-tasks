package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.PackageFacetBase;
import com.tomergabel.build.intellij.model.ResolutionException;
import com.tomergabel.build.intellij.model.WebFacet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ResourceCollection;

import java.io.File;

public class PackageWebFacetTask extends PackageFacetTaskBase<WebFacet> {
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

        final File webDescriptor;
        try {
            webDescriptor = resolver().resolveUriFile( facet.getWebDescriptorUrl() );
        } catch ( ResolutionException e ) {
            throw new BuildException( "Cannot resolve web descriptor file (web.xml) URL.", e );
        }

        if ( facet.isTargetEnabled() ) {
            final War war = (War) getProject().createTask( "war" );
            try {
                war.setDestFile( resolver().resolveUriFile( facet.getTargetUrl() ) );
                logVerbose( "Deleting package target %s", war.getDestFile() );
                war.getDestFile().delete();
            } catch ( ResolutionException e ) {
                throw new BuildException( "Cannot resolve WAR output URL.", e );
            }
            war.setWebxml( webDescriptor );
            war.add( rc );
            logInfo( "Packaging module %s to %s", module().getName(), war.getDestFile() );
            war.perform();
        }

        if ( facet.isExplodeEnabled() ) {
            final File explodeTarget;
            try {
                 explodeTarget = resolver().resolveUriFile( facet.getExplodedUrl() );
            } catch ( ResolutionException e ) {
                throw new BuildException( "Cannot resolve exploded WAR target URL.", e );
            }

            logInfo( "Copying module %s exploded output to %s", module().getName(), explodeTarget );
            
            // TODO copy task won't work because of the getName() hack we use in the various mappers. Find
            // TODO a proper way to do that; in the meantime hack around it.
//            final Copy copy = (Copy) getProject().createTask( "copy" );
//            copy.setTodir( explodeTarget );
//            copy.setOverwrite( true );
//            copy.add( rc );
////            copy.add( new AntUtils.SingletonResource( AntUtils.mapFileResource( webDescriptor, "WEB-INF/web.xml" ) ) );
//            logInfo( "Copying exploded module %s package to %s", module().getName(), explodeTarget );
//            copy.perform();

            // TODO temporary hack
            if ( !facet.isTargetEnabled() )
                throw new BuildException( "Web facets with exploded URLs but without target URLs " +
                        "are not currently supported." );

            final File war;
            try {
                 war = resolver().resolveUriFile( facet.getTargetUrl() );
            } catch ( ResolutionException e ) {
                throw new BuildException( "Cannot resolve exploded WAR target URL.", e );
            }

            final Expand expand = (Expand) getProject().createTask( "unzip" );
            expand.setSrc( war );
            expand.setDest( explodeTarget );
            expand.perform();
        }
    }

    protected ResourceCollection resolveWarResources( final WebFacet facet ) throws ResolutionException {
        if ( facet == null )
            throw new IllegalArgumentException( "The web facet cannot be null." );

        final Path path = new Path( getProject() );
        // Add packaging container
        path.add( resolvePackagingElements() );

        // Add source roots
        for ( final PackageFacetBase.Root source : facet.getSourceRoots() )
            path.add( resolveCompileOutput( resolver(), new PackageFacetBase.Root( source.getUrl(),
                    source.getTargetUri() != null ? source.getTargetUri() : "WEB-INF/classes" ) ) );

        // Add web roots
        for ( final PackageFacetBase.Root webRoot : facet.getWebRoots() ) {
            final FileSet fs = new FileSet();
            fs.setProject( getProject() );
            fs.setDir( resolver().resolveUriFile( webRoot.getUrl() ) );
            path.add( AntUtils.mapResources( fs, AntUtils.stripPreceedingSlash( webRoot.getTargetUri() ) ) );
        }

        return path;
    }
}
