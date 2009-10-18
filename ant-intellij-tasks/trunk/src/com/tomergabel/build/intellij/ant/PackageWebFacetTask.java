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
import com.tomergabel.build.intellij.model.ResolutionException;
import com.tomergabel.build.intellij.model.WebFacet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import java.io.File;

public class PackageWebFacetTask extends PackageFacetTaskBase<WebFacet> {
    protected File targetFile;

    public File getTargetFile() {
        return this.targetFile;
    }

    public void setTargetFile( final File targetFile ) {
        this.targetFile = targetFile;
    }

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
            tempDir = getTemporaryDirectory( "war" );
        } catch ( ResolutionException e ) {
            throw new BuildException( "Cannot create temporary target directory.", e );
        }

        packageWar( facet, tempDir );
        final FileSet fs = (FileSet) getProject().createDataType( "fileset" );
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
                final File target =
                        this.targetFile != null ? this.targetFile : resolver().resolveUriFile( facet.getTargetUrl() );
                war.setDestFile( target );
                logVerbose( "Deleting package target %s", war.getDestFile() );
                target.delete();
                target.getParentFile().mkdirs();
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
            ant().move( fs, explodeTarget );
            final Copy copy = (Copy) getProject().createTask( "copy" );
            final Path rc = (Path) getProject().createDataType( "path" );
            rc.setPath( webDescriptor.getAbsolutePath() );
            copy.add( rc );
            final File target = new File( explodeTarget, "WEB-INF/web.xml" );
            target.getParentFile().mkdirs();
            copy.setTofile( target );
            copy.perform();
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
                ant().compile( sourceDir, target, ant().buildModuleClasspath( resolver(), SourceFilter.source ) );
                ant().copy( ant().resolveModuleResources( resolver(), source.getUrl() ), target );
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
