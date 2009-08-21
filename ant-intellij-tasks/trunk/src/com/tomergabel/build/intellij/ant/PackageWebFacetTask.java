package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.PackageFacetBase;
import com.tomergabel.build.intellij.model.ResolutionException;
import com.tomergabel.build.intellij.model.WebFacet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.Union;

import java.io.File;
import java.io.IOException;

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

        // If neither WAR nor exploded target are specified, skip the whole thing
        if ( !facet.isExplodeEnabled() && !facet.isTargetEnabled() )
            return;

        // Generate temporary directory
        final File tempDir;
        try {
            tempDir = File.createTempFile( "idea", "war" );
        } catch ( IOException e ) {
            throw new BuildException( "Cannot create temporary target directory.", e );
        }
        tempDir.delete();   // Get rid of temporary file

        packageWar( facet, tempDir );
        final FileSet fs = (FileSet) getProject().createDataType( "FileSet" );
        fs.setDir( tempDir );
        fs.setIncludes( "**/*" );

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
            war.add( fs );
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
            final Union union = (Union) getProject().createDataType( "union" );
            union.add( fs );
            union.add( new AntUtils.SingletonResource( AntUtils.mapFileResource( webDescriptor, "WEB-INF/web.xml" ) ) );
            ant().move( union, explodeTarget );
        } else {
            logVerbose( "Deleting temporary directory " + tempDir );
            final Delete delete = (Delete) getProject().createTask( "delete" );
            delete.setDir( tempDir );
            delete.perform();
        }
    }

    protected void packageWar( final WebFacet facet, final File tempDir ) throws BuildException {
        if ( facet == null )
            throw new IllegalArgumentException( "The web facet cannot be null." );

        // Add packaging container
        packageContainerElements( tempDir );

        try {
            // Add source roots
            for ( final PackageFacetBase.Root source : facet.getSourceRoots() ) {
                final File target = source.getTargetUri() != null ? new File( tempDir,
                        AntUtils.stripPreceedingSlash( source.getTargetUri() ) )
                        : new File( tempDir, "WEB-INF/classes" );
                final File sourceDir = resolver().resolveUriFile( source.getUrl() );
                ant().compile( sourceDir, target );
                ant().copy( ant().resolveModuleSourceRoot( resolver(), source.getUrl(), false, true ), target );
            }

            // Add web roots
            for ( final PackageFacetBase.Root webRoot : facet.getWebRoots() ) {
                final File target = webRoot.getTargetUri() != null ? new File( tempDir,
                        AntUtils.stripPreceedingSlash( webRoot.getTargetUri() ) ) : tempDir;
                final FileSet fs = (FileSet) getProject().createDataType( "fileset" );
                fs.setDir( resolver().resolveUriFile( webRoot.getUrl() ) );
                fs.setIncludes( "**/*" );
                ant().copy( fs, target );
            }
        } catch ( ResolutionException e ) {
            throw new BuildException( e );
        }
    }
}
