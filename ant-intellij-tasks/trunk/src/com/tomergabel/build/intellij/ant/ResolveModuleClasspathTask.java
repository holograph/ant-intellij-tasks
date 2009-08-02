package com.tomergabel.build.intellij.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;

import java.util.Collection;

public class ResolveModuleClasspathTask extends ModuleTaskBase {
    protected String pathId;

    public void setPathId( final String pathId ) {
        this.pathId = pathId;
    }

    @Override
    public void executeTask() throws BuildException {
        if ( this.pathId == null )
            throw new BuildException( "Target path ID (attribute 'pathId') not specified." );

        // Resolve classpath
        final String moduleName = module().getName();
        final Collection<String> resolved;
        try {
            resolved = resolver().resolveModuleClasspath();
        } catch ( Exception e ) {
            throw new BuildException( "Cannot resolve module classpath for module \"" + moduleName + "\".", e );
        }

        // Create path object and add reference by name
        final Path classpath = new Path( getProject() );
        for ( final String path : resolved )
            classpath.append( new Path( getProject(), path ) );
        getProject().addReference( this.pathId, classpath );
    }
}
