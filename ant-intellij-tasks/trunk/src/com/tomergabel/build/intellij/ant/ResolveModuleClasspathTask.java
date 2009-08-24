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

        getProject().addReference( this.pathId, ant().buildClasspath( resolver() ) );
    }
}
