package com.tomergabel.build.intellij.ant;

import com.tomergabel.build.intellij.model.ResolutionException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;

import java.util.Collection;

public class ResolveModuleClasspathTask extends ModuleTaskBase {
    protected String pathId;

    public void setPathId( final String pathId ) {
        this.pathId = pathId;
    }

    @Override
    public void execute() throws BuildException {
        if ( this.pathId == null ) {
            error( "Target path ID (attribute 'pathId') not specified." );
            return;
        }

        // Resolve classpath
        final Collection<String> resolved;
        try {
            resolved = resolver().resolveModuleClasspath();
        } catch ( ResolutionException e ) {
            error( "Cannot resolve module classpath.", e );
            return;
        }

        // Create path object and add reference by name
        final Path classpath = new Path( getProject() );
        for ( String path : resolved )
            classpath.append( new Path( getProject(), path ) );
        getProject().addReference( this.pathId, classpath );
    }
}
