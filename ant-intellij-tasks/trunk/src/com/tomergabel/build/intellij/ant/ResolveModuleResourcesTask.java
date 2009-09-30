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

import com.tomergabel.build.intellij.model.Project;
import com.tomergabel.build.intellij.model.ResolutionException;
import com.tomergabel.util.CollectionUtils;
import com.tomergabel.util.UriUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import java.io.File;

public class ResolveModuleResourcesTask extends ModuleTaskBase {
    protected String pathId;
    protected SourceFilter filter = SourceFilter.both;

    public String getPathId() {
        return this.pathId;
    }

    public void setPathId( final String pathId ) {
        this.pathId = pathId;
    }

    public SourceFilter getFilter() {
        return this.filter;
    }

    public void setFilter( final SourceFilter filter ) {
        this.filter = filter;
    }

    @Override
    public void executeTask() throws BuildException {
        if ( this.pathId == null )
            throw new BuildException( "Target path (attribute 'pathid') not specified." );

        // Resolve resources
        getProject().addReference( this.pathId, resolveResourcePath() );
    }

    public Path resolveResourcePath() throws BuildException {
        final Path path = new Path( getProject() );
        final Project project = project();
        if ( project != null ) {
            final String[] includes = AntUtils.generateResourceIncludes( project );

            // Iterate source directories
            for ( final String sourceUrl : getSourceDirectories() ) {
                // Resolve source directory
                final File sourceDir;
                try {
                    sourceDir = UriUtils.getFile( resolver().resolveUriString( sourceUrl ) );
                } catch ( ResolutionException e ) {
                    throw new BuildException(
                            "Cannot resolve source directory for mdoule \"" + module().getName() + "\"", e );
                }

                // Create appropriate FileSet and add it to the output path
                final FileSet fileset = new FileSet();
                fileset.setDir( sourceDir );
                fileset.appendIncludes( includes );
                path.addFileset( fileset );
            }
        }
        return path;
    }

    public Iterable<String> getSourceDirectories() {
        // Choose a set of source directorise according to the mode
        switch ( this.filter ) {
            case source:
                return module().getSourceUrls();
            case test:
                return module().getTestSourceUrls();
            case both:
                return CollectionUtils.concat( module().getSourceUrls(), module().getTestSourceUrls() );
            default:
                // Safety net, should never happen
                throw new IllegalStateException( "Unrecognized filter \"" + this.filter.toString() + "\"" );
        }
    }
}
