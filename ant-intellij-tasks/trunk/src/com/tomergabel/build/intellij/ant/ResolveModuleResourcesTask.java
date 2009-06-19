package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.Module;
import com.tomergabel.build.intellij.model.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.util.ArrayList;

public class ResolveModuleResourcesTask extends ModuleTaskBase {
    protected String pathId;
    protected Filter filter = Filter.both;

    public String getPathId() {
        return pathId;
    }

    public void setPathId( final String pathId ) {
        this.pathId = pathId;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter( final Filter filter ) {
        this.filter = filter;
    }

    @Override
    public void execute() throws BuildException {
        if ( this.pathId == null ) {
            error( "Target path (attribute 'pathid') not specified." );
            return;
        }

        final Module module = module();
        if ( module == null )
            return;

        // Resolve resources
        final Path path = new Path( getProject() );
        final Project project = project();
        if ( project != null ) {
            // Derive resource patterns from project
            final FileSet fileset = new FileSet();
            fileset.setDir( new File( module.getModuleRoot() ) );

            final ArrayList<String> includes = new ArrayList<String>(
                    project.getResourceExtensions().size() + project.getResourceWildcardPatterns().size() );

            // Process resource extensions
            for ( String extension : project.getResourceExtensions() )
                includes.add( "**/*." + extension );

            // Process wildcard patterns
            for ( String pattern : project.getResourceWildcardPatterns() )
                includes.add( "**/" + pattern );

            fileset.appendIncludes( includes.toArray( new String[ includes.size() ] ) );
            path.addFileset( fileset );
        }

        getProject().addReference( this.pathId, path );
    }
}
